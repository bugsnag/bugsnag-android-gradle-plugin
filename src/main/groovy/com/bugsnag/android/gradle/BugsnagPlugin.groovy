package com.bugsnag.android.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ManifestProcessorTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

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

    public static final String GROUP_NAME = "Bugsnag"
    private static final String NDK_PROJ_TASK = "externalNative"
    private static final String CLEAN_TASK = "Clean"
    private static final String ASSEMBLE_TASK = "assemble"
    private static final String BUNDLE_TASK = "bundle"

    void apply(Project project) {
        project.extensions.create("bugsnag", BugsnagPluginExtension)
        project.bugsnag.extensions.create("sourceControl", SourceControl)

        project.afterEvaluate {
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
                throw new IllegalStateException("Must apply \'com.android.application\' first!")
            }

            if (isNdkProject(project)) {
                setupNdkProject(project)
            }
        }
    }

    private static void setupNdkProject(Project project) {
        Set<Task> cleanTasks = project.tasks.findAll {
            it.name.startsWith(NDK_PROJ_TASK) && it.name.contains(CLEAN_TASK)
        }
        Set<Task> buildTasks = project.tasks.findAll {
            it.name.startsWith(NDK_PROJ_TASK) && !it.name.contains(CLEAN_TASK)
        }

        project.tasks.register("bugsnagInstallJniLibsTask", BugsnagNdkSetupTask) { ndkSetupTask ->
            buildTasks.forEach {
                ndkSetupTask.mustRunAfter(cleanTasks)
                it.dependsOn ndkSetupTask
                it.doFirst { ndkSetupTask }
            }
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

        variant.outputs.each { output ->
            BugsnagTaskDeps deps = new BugsnagTaskDeps()
            deps.variant = variant
            deps.output = output

            setupManifestUuidTask(project, deps)

            if (variant.buildType.minifyEnabled || hasDexguardPlugin(project)) {
                setupMappingFileUpload(project, deps)
            }

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
        String taskName = "uploadBugsnag${taskNameForOutput(deps.output)}Mapping"
        project.tasks.register(taskName, BugsnagUploadProguardTask) { uploadTask ->
            uploadTask.partName = "proguard"
            prepareUploadTask(uploadTask, deps, project)
        }
    }

    private static void setupNdkMappingFileUpload(Project project, BugsnagTaskDeps deps) {
        if (isNdkProject(project)) {
            // Create a Bugsnag task to upload NDK mapping file(s)
            String taskName = "uploadBugsnagNdk${taskNameForOutput(deps.output)}Mapping"
            project.tasks.register(taskName, BugsnagUploadNdkTask) { uploadNdkTask ->
                prepareUploadTask(uploadNdkTask, deps, project)

                uploadNdkTask.variantName = taskNameForVariant(deps.variant)
                uploadNdkTask.projectDir = project.projectDir
                uploadNdkTask.rootDir = project.rootDir
                uploadNdkTask.sharedObjectPath = project.bugsnag.sharedObjectPath
            }
        }
    }

    private static boolean isNdkProject(Project project) {
        if (project.bugsnag.ndk != null) { // always respect user override
            return project.bugsnag.ndk
        } else { // infer whether native build or not
            boolean usesCmake = project.android.externalNativeBuild.cmake.path != null
            boolean usesNdkBuild = project.android.externalNativeBuild.ndkBuild.path != null
            return usesCmake || usesNdkBuild
        }
    }

    private static void setupReleasesTask(Project project, BugsnagTaskDeps deps) {
        String taskName = "bugsnagRelease${taskNameForOutput(deps.output)}Task"
        BugsnagReleasesTask releasesTask = project.tasks.create(taskName, BugsnagReleasesTask)
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

    private static void setupBugsnagTask(Task task, BugsnagTaskDeps deps) {
        task.group = GROUP_NAME
        task.variantOutput = deps.output
        task.variant = deps.variant
    }

    private static void prepareUploadTask(Task uploadTask, BugsnagTaskDeps deps, Project project) {
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
        taskNames.addAll(findTaskNamesForPrefix(variant, output, ASSEMBLE_TASK))
        taskNames.addAll(findTaskNamesForPrefix(variant, output, BUNDLE_TASK))

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
        Task assembleTask = variant.assembleProvider.getOrNull()

        Set<String> taskNames = new HashSet<>()
        taskNames.add(prefix)

        if (assembleTask != null) {
            String assembleTaskName = assembleTask.name
            String buildTypeTaskName = assembleTaskName.replaceAll(variantName, "")
            String buildType = buildTypeTaskName.replaceAll(ASSEMBLE_TASK, "")
            String variantTaskName = assembleTaskName.replaceAll(buildType, "")

            taskNames.add(assembleTaskName.replaceAll(ASSEMBLE_TASK, prefix))
            taskNames.add(buildTypeTaskName.replaceAll(ASSEMBLE_TASK, prefix))
            taskNames.add(variantTaskName.replaceAll(ASSEMBLE_TASK, prefix))
        }
        taskNames
    }

    private static void setupManifestUuidTask(Project project, BugsnagTaskDeps deps) {
        String taskName = "processBugsnag${taskNameForOutput(deps.output)}Manifest"
        project.tasks.register(taskName, BugsnagManifestTask) { manifestTask ->
            setupBugsnagTask(manifestTask, deps)
            ManifestProcessorTask processManifest = deps.output.processManifestProvider.getOrNull()

            if (processManifest == null) {
                return
            }

            processManifest.finalizedBy(manifestTask)
            manifestTask.dependsOn(processManifest)

            Set<Task> resourceTasks = project.tasks.findAll {
                String name = it.name.toLowerCase()
                name.startsWith(BUNDLE_TASK) && name.endsWith("resources")
            }

            resourceTasks.forEach {
                it.dependsOn manifestTask
            }
        }
    }

    static File resolveBundleManifestOutputDirectory(ManifestProcessorTask processManifest) {
        // For AGP versions >= 3.3.0 the bundle manifest is output to its own directory
        def directory = processManifest.bundleManifestOutputDirectory

        if (directory instanceof File) { // 3.3.X - 3.5.X returns a File
            return directory
        } else { // 3.6.+ returns a DirectoryProperty
            return directory.asFile.getOrNull()
        }
    }

    static String taskNameForVariant(BaseVariant variant) {
        variant.name.capitalize()
    }

    static String taskNameForOutput(BaseVariantOutput output) {
        output.name.capitalize()
    }

    private static boolean hasDisabledBugsnag(BaseVariant variant) {
        Closure<Boolean> hasDisabledBugsnag = {
            it.ext.properties.containsKey("enableBugsnag") && !it.ext.enableBugsnag
        }

        // Ignore any conflicting properties, bail if anything has a disable flag.
        (variant.productFlavors + variant.buildType).any(hasDisabledBugsnag)
    }

    /**
     * Returns true if the DexGuard plugin has been applied to the project
     */
    static boolean hasDexguardPlugin(Project project) {
        project.pluginManager.hasPlugin("dexguard")
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
        outputSize > variantSize
    }

    /**
     * Whether or not an assemble task is going to be run for this variant
     */
    static boolean isRunningAssembleTask(BaseVariant variant, BaseVariantOutput output, Project project) {
        isRunningTaskWithPrefix(variant, output, project, ASSEMBLE_TASK)
    }

    /**
     * Whether or not a bundle task is going to be run for this variant
     */
    static boolean isRunningBundleTask(BaseVariant variant, BaseVariantOutput output, Project project) {
        isRunningTaskWithPrefix(variant, output, project, BUNDLE_TASK)
    }

    /**
     * Whether or any of a list of the task names for a prefix are going to be run by checking the list
     * against all of the tasks in the task graph
     */
    private static boolean isRunningTaskWithPrefix(BaseVariant variant,
                                                   BaseVariantOutput output, Project project, String prefix) {
        Set<String> taskNames = new HashSet<>()
        taskNames.addAll(findTaskNamesForPrefix(variant, output, prefix))

        project.gradle.taskGraph.allTasks.any { task ->
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
