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
        setupSplitsDiscovery(project, variant)

        variant.outputs.each { output ->
            if (!output.name.toLowerCase().endsWith("debug") || project.bugsnag.uploadDebugBuildMappings) {
                setupManifestUuidTask(project, variant, output)
                setupMappingFileUpload(project, variant, output)
                setupNdkMappingFileUpload(project, variant, output)
                setupReleasesTask(project, variant, output)
            }
        }
    }

    private static void setupSplitsDiscovery(Project project, BaseVariant variant) {
        BugsnagSplitsInfoTask splitsInfoTask = project.tasks.create("bugsnagSplitsInfo${taskNameForVariant(variant)}", BugsnagSplitsInfoTask)
        splitsInfoTask.group = GROUP_NAME
        splitsInfoTask.variant = variant
        dependTaskOnPackageTask(variant, splitsInfoTask)
        splitsInfoTask.mustRunAfter variant.outputs.first().processManifest
    }

    /**
     * Creates a bugsnag task to upload proguard mapping file
     */
    private static void setupMappingFileUpload(Project project, BaseVariant variant, BaseVariantOutput output) {
        def uploadTask = project.tasks.create("uploadBugsnag${taskNameForOutput(output)}Mapping", BugsnagUploadProguardTask)
        uploadTask.partName = isJackEnabled(project, variant) ? "jack" : "proguard"
        prepareUploadTask(uploadTask, output, variant, project)
    }

    private static void setupNdkMappingFileUpload(Project project, BaseVariant variant, BaseVariantOutput output) {
        if (project.bugsnag.ndk) {
            File symbolPath = getSymbolPath(output)
            File intermediatePath = getIntermediatePath(symbolPath)

            // Create a Bugsnag task to upload NDK mapping file(s)
            BugsnagUploadNdkTask uploadNdkTask = project.tasks.create("uploadBugsnagNdk${taskNameForOutput(output)}Mapping", BugsnagUploadNdkTask)
            prepareUploadTask(uploadNdkTask, output, variant, project)

            uploadNdkTask.intermediatePath = intermediatePath
            uploadNdkTask.symbolPath = symbolPath
            uploadNdkTask.variantName = taskNameForVariant(variant)
            uploadNdkTask.projectDir = project.projectDir
            uploadNdkTask.rootDir = project.rootDir
            uploadNdkTask.toolchain = getCmakeToolchain(project, variant)
            uploadNdkTask.sharedObjectPath = project.bugsnag.sharedObjectPath
        }
    }

    private static void setupReleasesTask(Project project, BaseVariant variant, BaseVariantOutput output) {
        def releasesTask = project.tasks.create("bugsnagRelease${taskNameForOutput(output)}Task", BugsnagReleasesTask)
        releasesTask.variantOutput = output
        releasesTask.variant = variant

        def buildTask = project.tasks.findByName("build")
        releasesTask.mustRunAfter buildTask
        buildTask.finalizedBy releasesTask // FIXME handle upload task finalization
    }

    private static void prepareUploadTask(uploadTask, BaseVariantOutput output, BaseVariant variant, Project project) {
        uploadTask.group = GROUP_NAME
        uploadTask.variantOutput = output
        uploadTask.variant = variant
        uploadTask.applicationId = variant.applicationId


        // Expected behaviour:
        // assemble
        // assembleJavaExampleRelease
        // assembleJavaExample
        // assembleRelease


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
        .forEach {
            uploadTask.mustRunAfter it

            if (project.bugsnag.autoUpload) {
                it.finalizedBy uploadTask
            }
        }
    }

    private static void setupManifestUuidTask(Project project, BaseVariant variant, BaseVariantOutput output) {
        BugsnagManifestTask manifestTask = project.tasks.create("processBugsnag${taskNameForOutput(output)}Manifest", BugsnagManifestTask)
        manifestTask.variantOutput = output
        manifestTask.variant = variant
        manifestTask.group = GROUP_NAME
        manifestTask.mustRunAfter output.processManifest
        dependTaskOnPackageTask(variant, manifestTask)
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

    private static File getIntermediatePath(File symbolPath) {
        def intermediatePath = null

        if (symbolPath != null) {
            intermediatePath = symbolPath.parentFile.parentFile
        }
        intermediatePath
    }

    private static File getSymbolPath(BaseVariantOutput variantOutput) {
        def symbolPath = variantOutput.processResources.textSymbolOutputFile

        if (symbolPath == null) {
            throw new IllegalStateException("Could not find symbol path")
        }
        symbolPath
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
}
