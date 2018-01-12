package com.bugsnag.android.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.core.Toolchain
import com.android.build.gradle.internal.dsl.BuildType
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

    static final String API_KEY_TAG = 'com.bugsnag.android.API_KEY'
    static final String BUILD_UUID_TAG = 'com.bugsnag.android.BUILD_UUID'
    static final String GROUP_NAME = 'Bugsnag'

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
                throw new IllegalStateException('Must apply \'com.android.application\' first!')
            }

            if (isNdkProject(project)) {
                setupNdkProject(project)
            }
        }
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
    private static void applyBugsnagToVariant(BaseVariant variant, Project project) {
        if (hasDisabledBugsnag(variant)) {
            return
        }

        // only need to be run once per variant
        setupProguardAutoConfig(project, variant)
        def splitsTask = setupSplitsDiscovery(project, variant)

        variant.outputs.each { output ->
            if (!output.name.toLowerCase().endsWith("debug") || project.bugsnag.uploadDebugBuildMappings) {
                BugsnagTaskDeps deps = new BugsnagTaskDeps()
                deps.variant = variant
                deps.output = output
                deps.splitsTask = splitsTask

                setupManifestUuidTask(project, deps)
                setupMappingFileUpload(project, deps)
                setupNdkMappingFileUpload(project, deps)
                setupReleasesTask(project, deps)
            }
        }
    }

    private static def setupSplitsDiscovery(Project project, BaseVariant variant) {
        BugsnagSplitsInfoTask splitsInfoTask = project.tasks.create("bugsnagSplitsInfo${taskNameForVariant(variant)}", BugsnagSplitsInfoTask)
        splitsInfoTask.group = GROUP_NAME
        splitsInfoTask.variant = variant
        dependTaskOnPackageTask(variant, splitsInfoTask)
        splitsInfoTask.mustRunAfter variant.outputs.first().processManifest
        splitsInfoTask
    }

    /**
     * Creates a bugsnag task to upload proguard mapping file
     */
    private static void setupMappingFileUpload(Project project, BugsnagTaskDeps deps) {
        def uploadTask = project.tasks.create("uploadBugsnag${taskNameForOutput(deps.output)}Mapping", BugsnagUploadProguardTask)
        uploadTask.partName = isJackEnabled(project, deps.variant) ? "jack" : "proguard"
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
            uploadNdkTask.toolchain = getCmakeToolchain(project, deps.variant)
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

        findAssembleTasks(deps.output, project).forEach {
            releasesTask.mustRunAfter it

            if (project.bugsnag.autoReportBuilds) {
                it.finalizedBy releasesTask
            }
        }
    }

    private static def setupBugsnagTask(BugsnagVariantOutputTask task, BugsnagTaskDeps deps) {
        task.group = GROUP_NAME
        task.variantOutput = deps.output
        task.variant = deps.variant
        task.dependsOn deps.splitsTask
    }

    private static void prepareUploadTask(BugsnagMultiPartUploadTask uploadTask, BugsnagTaskDeps deps, Project project) {
        setupBugsnagTask(uploadTask, deps)
        uploadTask.applicationId = deps.variant.applicationId

        findAssembleTasks(deps.output, project).forEach {
            uploadTask.mustRunAfter it

            if (project.bugsnag.autoUpload) {
                it.finalizedBy uploadTask
            }
        }
    }

    /**
     * Fetches all the assemble tasks in the current project that match the variant
     *
     * Expected behaviour: assemble, assembleJavaExampleRelease, assembleJavaExample, assembleRelease
     *
     * @param output the variantOutput
     * @param project the current project
     * @return the assemble tasks
     */
    private static Set<Task> findAssembleTasks(BaseVariantOutput output, Project project) {
        String variantName = output.name.split("-")[0].capitalize()
        String assembleTaskName = output.assemble.name
        String buildTypeTaskName = assembleTaskName.replaceAll(variantName, "")
        String buildType = buildTypeTaskName.replaceAll("assemble", "")
        String variantTaskName = assembleTaskName.replaceAll(buildType, "")

        Set<String> taskNames = new HashSet<>()
        taskNames.add(assembleTaskName)
        taskNames.add("assemble")
        taskNames.add(buildTypeTaskName)
        taskNames.add(variantTaskName)

        project.tasks.findAll {
            taskNames.contains(it.name)
        }
    }

    private static void setupManifestUuidTask(Project project, BugsnagTaskDeps deps) {
        BugsnagManifestTask manifestTask = project.tasks.create("processBugsnag${taskNameForOutput(deps.output)}Manifest", BugsnagManifestTask)
        setupBugsnagTask(manifestTask, deps)
        manifestTask.mustRunAfter deps.output.processManifest
        dependTaskOnPackageTask(deps.variant, manifestTask)
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
    private static void setupProguardAutoConfig(Project project, BaseVariant variant) {
        BugsnagProguardConfigTask proguardConfigTask = project.tasks.create("processBugsnag${taskNameForVariant(variant)}Proguard", BugsnagProguardConfigTask)
        proguardConfigTask.group = GROUP_NAME
        proguardConfigTask.variant = variant

        if (project.bugsnag.autoProguardConfig) {
            project.logger.debug("Bugsnag autoproguard config enabled")
            dependTaskOnPackageTask(variant, proguardConfigTask)
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
            variant.getPackageApplication().dependsOn task
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
     * Checks to see if the Jack compiler is being used for the given variant
     *
     * @param project The project to check in
     * @param variant The variant to check
     * @return true if Jack is enabled, else false
     */
    private static boolean isJackEnabled(Project project, BaseVariant variant) {

        // First check the selected build type to see if there are jack settings
        TreeSet buildTypes = project.android.buildTypes.store
        BuildType b = findNode(buildTypes, variant.baseName)

        if (b?.hasProperty('jackOptions')
            && b.jackOptions.enabled instanceof Boolean) {

            return b.jackOptions.enabled

            // Now check the default config to see if any Jack settings are defined
        } else if (project.android.defaultConfig?.hasProperty('jackOptions')
            && project.android.defaultConfig.jackOptions.enabled instanceof Boolean) {

            return project.android.defaultConfig.jackOptions.enabled
        } else {
            return false
        }
    }

    /**
     * Gets the buildchain that is setup for cmake
     * @param project The project to check
     * @param variant The variant to check
     * @return The buildchain for cmake (or Toolchain.default if not found)
     */
    private static String getCmakeToolchain(Project project, BaseVariant variant) {
        String toolchain = null

        // First check the selected build type to see if there are cmake arguments
        TreeSet buildTypes = project.android.buildTypes.store
        BuildType b = findNode(buildTypes, variant.baseName)

        if (b != null
            && b.externalNativeBuildOptions != null
            && b.externalNativeBuildOptions.cmake != null
            && b.externalNativeBuildOptions.cmake.arguments != null) {

            ArrayList<String> args = b.externalNativeBuildOptions.cmake.arguments
            toolchain = getToolchain(args)
        }

        // Next check to see if there are arguments in the default config section
        if (toolchain == null) {
            if (project.android.defaultConfig.externalNativeBuildOptions != null
                && project.android.defaultConfig.externalNativeBuildOptions.cmake != null
                && project.android.defaultConfig.externalNativeBuildOptions.cmake.arguments != null) {

                ArrayList<String> args = project.android.defaultConfig.externalNativeBuildOptions.cmake.arguments
                for (String arg : args) {
                    toolchain = getToolchain(args)
                }
            }
        }

        // Default to Toolchain.default if not found so far
        if (toolchain == null) {
            toolchain = Toolchain.default.name
        }

        return toolchain
    }

    /**
     * Looks for an "ANDROID_TOOLCHAIN" argument in a list of cmake arguments
     * @param args The cmake args
     * @return the value of the "ANDROID_TOOLCHAIN" argument, or null if not found
     */
    private static String getToolchain(ArrayList<String> args) {
        for (String arg : args) {
            if (arg.startsWith("-DANDROID_TOOLCHAIN")) {
                return arg.substring(arg.indexOf("=") + 1).trim()
            }
        }
        return null
    }

    /**
     * Finds the given build type in a TreeSet of buildtypes
     * @param set The TreeSet of build types
     * @param name The name of the buildtype to search for
     * @return The buildtype, or null if not found
     */
    private static BuildType findNode(TreeSet<BuildType> set, String name) {
        Iterator<BuildType> iterator = set.iterator()

        while (iterator.hasNext()) {
            BuildType node = iterator.next()
            if (node.getName() == name) {
                return node
            }
        }
        return null
    }

    private static class BugsnagTaskDeps {
        BaseVariant variant
        BaseVariantOutput output
        def splitsTask
    }
}
