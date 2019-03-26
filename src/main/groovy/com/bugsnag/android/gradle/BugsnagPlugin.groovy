package com.bugsnag.android.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.tasks.ManifestProcessorTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.util.VersionNumber

/**
 * Gradle plugin to automatically upload ProGuard mapping files to Bugsnag.
 *
 * This plugin creates Gradle Tasks, and hooks them into a typical build
 * process. Knowledge of the Android build lifecycle is required to
 * understand how we attach tasks as dependencies.
 *
 * Run `gradle tasks --all` in an Android app project to see all tasks and
 * dependencies.
 *
 * Further reading:
 * https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Tasks
 * https://docs.gradle.org/current/userguide/custom_tasks.html
 */
class BugsnagPlugin implements Plugin<Project> {

    static final String API_KEY_TAG = 'com.bugsnag.android.API_KEY'
    static final String BUILD_UUID_TAG = 'com.bugsnag.android.BUILD_UUID'
    static final String GROUP_NAME = 'Bugsnag'

    VersionNumber bugsnagVersionNumber

    void apply(Project project) {
        project.extensions.create("bugsnag", BugsnagPluginExtension)
        project.bugsnag.extensions.create("sourceControl", SourceControl)

        project.afterEvaluate {
            bugsnagVersionNumber = getBugsnagAndroidVersionNumber(project)
            project.logger.debug("Using bugsnag-android version number: $bugsnagVersionNumber")

            // Make sure the android plugin has been applied first
            if (project.plugins.hasPlugin(AppPlugin)) {
                project.android.applicationVariants.all { variant ->
                    applyBugsnagToVariant(variant, project)
                }
            } else if (project.plugins.hasPlugin(LibraryPlugin)) {
                project.android.libraryVariants.all  { variant ->
                    applyBugsnagToVariant(variant, project)
                }
            } else {
                throw new IllegalStateException('Must apply \'com.android.application\' first!')
            }

            if (isNdkProject(project)) {
                setupNdkProject(project)
            }
        }
    }

    /**
     * Retrieves the VersionNumber used by com.bugsnag.android in the given project. This can be used
     * to conditionally perform tasks depending on the artefact version.
     */
    static VersionNumber getBugsnagAndroidVersionNumber(Project project) {
        List<Configuration> configs = project.configurations.collect()
        List<Dependency> deps = configs.stream()
            .map { conf -> conf.allDependencies }
            .collect()
            .flatten()

        def bugsnagVersion = deps.stream()
            .filter { dep -> return dep.group == "com.bugsnag" && dep.name == "bugsnag-android" }
            .distinct()
            .map({ dep -> return dep.version })
            .findFirst()

        return bugsnagVersion.present ? VersionNumber.parse(bugsnagVersion.get()) : VersionNumber.UNKNOWN
    }

    private static void setupNdkProject(Project project) {
        def cleanTasks = project.tasks.findAll {
            it.name.startsWith("externalNative") && it.name.contains("Clean")
        }
        def buildTasks = project.tasks.findAll {
            it.name.startsWith("externalNative") && !it.name.contains("Clean")
        }

        def ndkSetupTask = project.tasks.create("bugsnagInstallJniLibsTask", BugsnagNdkSetupTask)

        buildTasks.forEach {
            ndkSetupTask.mustRunAfter(cleanTasks)
            it.dependsOn ndkSetupTask
            it.doFirst { ndkSetupTask }
        }
    }

    /**
     * Create tasks for each Build Variant
     * See https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Variants
     */
    private void applyBugsnagToVariant(BaseVariant variant, Project project) {
        if (hasDisabledBugsnag(variant)) {
            return
        }

        // only need to be run once per variant
        setupProguardAutoConfig(project, variant)

        variant.outputs.each { output ->
            if (!variant.buildType.minifyEnabled && !hasDexguardPlugin(project)) {
                return
            }

            BugsnagTaskDeps deps = new BugsnagTaskDeps()
            deps.variant = variant
            deps.output = output

            setupManifestUuidTask(project, deps)
            setupMappingFileUpload(project, deps)
            setupNdkMappingFileUpload(project, deps)
            setupReleasesTask(project, deps)
        }
    }

    private static boolean shouldUploadDebugMappings(Project project, BaseVariantOutput output) {
        !output.name.toLowerCase().endsWith("debug") || project.bugsnag.uploadDebugBuildMappings
    }

    /**
     * Creates a bugsnag task to upload proguard mapping file
     */
    private static void setupMappingFileUpload(Project project, BugsnagTaskDeps deps) {
        def uploadTask = project.tasks.create("uploadBugsnag${taskNameForOutput(deps.output)}Mapping", BugsnagUploadProguardTask)
        uploadTask.partName = "proguard"
        prepareUploadTask(uploadTask, deps, project)
    }

    private static void setupNdkMappingFileUpload(Project project, BugsnagTaskDeps deps) {
        if (isNdkProject(project)) {
            // Create a Bugsnag task to upload NDK mapping file(s)
            BugsnagUploadNdkTask uploadNdkTask = project.tasks.create("uploadBugsnagNdk${taskNameForOutput(deps.output)}Mapping", BugsnagUploadNdkTask)
            prepareUploadTask(uploadNdkTask, deps, project)

            uploadNdkTask.variantName = taskNameForVariant(deps.variant)
            uploadNdkTask.projectDir = project.projectDir
            uploadNdkTask.rootDir = project.rootDir
            uploadNdkTask.sharedObjectPath = project.bugsnag.sharedObjectPath
        }
    }

    private static boolean isNdkProject(Project project) {
        if (project.bugsnag.ndk != null) { // always respect user override
            return project.bugsnag.ndk
        } else { // infer whether native build or not
            def tasks = project.tasks.findAll()
            return tasks.stream().anyMatch {
                it.name.startsWith("externalNative")
            }
        }
    }

    private static void setupReleasesTask(Project project, BugsnagTaskDeps deps) {
        def releasesTask = project.tasks.create("bugsnagRelease${taskNameForOutput(deps.output)}Task", BugsnagReleasesTask)
        setupBugsnagTask(releasesTask, deps)

        if (shouldUploadDebugMappings(project, deps.output)) {
            findAssembleBundleTasks(deps.variant, deps.output, project).forEach {
                releasesTask.mustRunAfter it

                if (project.bugsnag.autoReportBuilds) {
                    it.finalizedBy releasesTask
                }
            }
        }
    }

    private static def setupBugsnagTask(BugsnagVariantOutputTask task, BugsnagTaskDeps deps) {
        task.group = GROUP_NAME
        task.variantOutput = deps.output
        task.variant = deps.variant
    }

    private static void prepareUploadTask(BugsnagMultiPartUploadTask uploadTask, BugsnagTaskDeps deps, Project project) {
        setupBugsnagTask(uploadTask, deps)
        uploadTask.applicationId = deps.variant.applicationId

        if (shouldUploadDebugMappings(project, deps.output)) {
            findAssembleBundleTasks(deps.variant, deps.output, project).forEach {
                uploadTask.mustRunAfter it

                if (project.bugsnag.autoUpload) {
                    it.finalizedBy uploadTask
                }
            }
        }
    }

    /**
     * Fetches all the assemble and bundle tasks in the current project that match the variant
     *
     * Expected behaviour: [assemble, assembleJavaExampleRelease, assembleJavaExample, assembleRelease,
     * bundle, bundleJavaExampleRelease, bundleJavaExample, bundleRelease]
     *
     * @param output the variantOutput
     * @param project the current project
     * @return the assemble + bundle tasks
     */
    private static Set<Task> findAssembleBundleTasks(BaseVariant variant, BaseVariantOutput output, Project project) {
        Set<String> taskNames = new HashSet<>()
        taskNames.addAll(findTaskNamesForPrefix(variant, output, "assemble"))
        taskNames.addAll(findTaskNamesForPrefix(variant, output, "bundle"))

        project.tasks.findAll {
            taskNames.contains(it.name)
        }
    }

    /**
     * Finds all the task names which can be used to assemble a variant, and replaces 'assemble' with the given
     * prefix.
     *
     * E.g. [bundle, bundleRelease, bundleFooRelease]
     */
    private static Set<String> findTaskNamesForPrefix(BaseVariant variant, BaseVariantOutput output, String prefix) {
        String variantName = output.name.split("-")[0].capitalize()
        def assembleTask = resolveAssembleTask(variant)
        String assembleTaskName = assembleTask.name
        String buildTypeTaskName = assembleTaskName.replaceAll(variantName, "")
        String buildType = buildTypeTaskName.replaceAll("assemble", "")
        String variantTaskName = assembleTaskName.replaceAll(buildType, "")

        Set<String> taskNames = new HashSet<>()
        taskNames.add(prefix)
        taskNames.add(assembleTaskName.replaceAll("assemble", prefix))
        taskNames.add(buildTypeTaskName.replaceAll("assemble", prefix))
        taskNames.add(variantTaskName.replaceAll("assemble", prefix))
        return taskNames
    }

    private static def resolveAssembleTask(BaseVariant variant) {
        try {
            return variant.assembleProvider.get()
        } catch (Throwable ignored) {
            return variant.assemble
        }
    }

    private static void setupManifestUuidTask(Project project, BugsnagTaskDeps deps) {
        BugsnagManifestTask manifestTask = project.tasks.create("processBugsnag${taskNameForOutput(deps.output)}Manifest", BugsnagManifestTask)
        setupBugsnagTask(manifestTask, deps)
        ManifestProcessorTask processManifest = resolveProcessManifest(deps.output)

        processManifest.finalizedBy(manifestTask)
        manifestTask.dependsOn(processManifest)

        def resourceTasks = project.tasks.findAll {
            def name = it.name.toLowerCase()
            name.contains("bundle") && name.contains("resources")
        }

        resourceTasks.forEach {
            it.dependsOn manifestTask
        }
    }

    static def resolveProcessManifest(BaseVariantOutput output) {
        try {
            return output.processManifestProvider.get()
        } catch (Throwable ignored) {
            return output.processManifest
        }
    }

    static File resolveBundleManifestOutputDirectory(ManifestProcessorTask processManifest) {
        if (processManifest.hasProperty("bundleManifestOutputDirectory")) {
            // For AGP versions >= 3.3.0 the bundle manifest is output to its own directory
            return processManifest.bundleManifestOutputDirectory
        } else {
            // For AGP versions < 3.3.0 the bundle manifest is the merged manifest
            return processManifest.manifestOutputDirectory
        }
    }

    /**
     * Automatically add the "edit proguard settings" task to the
     * build process.
     *
     * This task must be called before ProGuard is run, but since
     * the name of the ProGuard task changed between 1.0 and 1.5
     * of the Android build tools, we'll hook into the "package"
     * task as a dependency, since this is always run before
     * ProGuard.
     *
     * For reference, in Android Build Tools 1.0, the ProGuard
     * task was named `proguardRelease`, and in 1.5+ the ProGuard
     * task is named `transformClassesAndResourcesWithProguardForRelease`
     * as it is now part of the "transforms" process.
     */
    private void setupProguardAutoConfig(Project project, BaseVariant variant) {
        BugsnagProguardConfigTask proguardConfigTask = project.tasks.create("processBugsnag${taskNameForVariant(variant)}Proguard", BugsnagProguardConfigTask)
        proguardConfigTask.group = GROUP_NAME
        proguardConfigTask.variant = variant

        // consumer proguard rules were added to the library in 4.6.0
        boolean hasConsumerRules = bugsnagVersionNumber.major >= 4 && bugsnagVersionNumber.minor >= 6

        if (project.bugsnag.autoProguardConfig && !hasConsumerRules) {
            project.logger.debug("Bugsnag autoproguard config enabled")
            dependTaskOnPackageTask(variant, proguardConfigTask)
        } else {
            project.logger.debug("ProGuard has consumer rules, skipping write")
        }
    }

    static String taskNameForVariant(BaseVariant variant) {
        variant.name.capitalize()
    }

    static String taskNameForOutput(BaseVariantOutput output) {
        output.name.capitalize()
    }

    private static void dependTaskOnPackageTask(BaseVariant variant, Task task) {
        if (variant instanceof LibraryVariant) {
            variant.getPackageLibrary().dependsOn task
        } else {
            resolvePackageApplication(variant).dependsOn task
        }
    }

    static def resolvePackageApplication(BaseVariant variant) {
        try {
            return variant.getPackageApplicationProvider().get()
        } catch (Throwable ignored) {
            return variant.getPackageApplication()
        }
    }

    private static boolean hasDisabledBugsnag(BaseVariant variant) {
        def hasDisabledBugsnag = {
            it.ext.properties.containsKey("enableBugsnag") && !it.ext.enableBugsnag
        }

        // Ignore any conflicting properties, bail if anything has a disable flag.
        return (variant.productFlavors + variant.buildType).any(hasDisabledBugsnag)
    }

    /**
     * Returns true if the DexGuard plugin has been applied to the project
     */
    static boolean hasDexguardPlugin(Project project) {
        return project.pluginManager.hasPlugin("dexguard")
    }

    /**
     * Returns true if a project has configured multiple variant outputs.
     *
     * This calculation is based on a heuristic - the number of variantOutputs in a project must be
     * greater than the number of variants.
     */
    static boolean hasMultipleOutputs(Project project) {
        DomainObjectSet<ApplicationVariant> variants = project.android.applicationVariants
        int variantSize = variants.size()
        int outputSize = 0

        variants.forEach { variant ->
            outputSize += variant.outputs.size()
        }
        return outputSize > variantSize
    }

    /**
     * Whether or not an assemble task is going to be run for this variant
     */
    static boolean isRunningAssembleTask(BaseVariant variant, BaseVariantOutput output, Project project) {
        return isRunningTaskWithPrefix(variant, output, project, "assemble")
    }

    /**
     * Whether or not a bundle task is going to be run for this variant
     */
    static boolean isRunningBundleTask(BaseVariant variant, BaseVariantOutput output, Project project) {
        return isRunningTaskWithPrefix(variant, output, project, "bundle")
    }

    /**
     * Whether or any of a list of the task names for a prefix are going to be run by checking the list
     * against all of the tasks in the task graph
     */
    private static boolean isRunningTaskWithPrefix(BaseVariant variant, BaseVariantOutput output, Project project, String prefix) {
        Set<String> taskNames = new HashSet<>()
        taskNames.addAll(findTaskNamesForPrefix(variant, output, prefix))

        return project.gradle.taskGraph.getAllTasks().any { task ->
            taskNames.any {
                task.name.endsWith(it)
            }
        }
    }

    private static class BugsnagTaskDeps {
        BaseVariant variant
        BaseVariantOutput output
    }
}
