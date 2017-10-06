package com.bugsnag.android.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.core.Toolchain
import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.Plugin
import org.gradle.api.Project
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

    private BugsnagManifestTask manifestUuidTask
    private BugsnagUploadProguardTask uploadProguardTask
    private BugsnagUploadNdkTask uploadNdkTask

    class SplitsInfo {
        def densityFilters
        def languageFilters
        def abiFilters
    }

    void apply(Project project) {
        project.extensions.create("bugsnag", BugsnagPluginExtension)

        project.afterEvaluate {
            // Make sure the android plugin has been applied first
            if (!project.plugins.hasPlugin(AppPlugin)) {
                throw new IllegalStateException('Must apply \'com.android.application\' first!')
            }

            project.android.applicationVariants.all { ApplicationVariant variant ->
                applyBugsnagToVariant(variant, project)
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

        // only need to be run once per variant
        setupProguardAutoConfig(project, variant)
        setupSplitsDiscovery(project, variant)

        // need to be run for each output
        variant.outputs.all { output ->
            this.manifestUuidTask = setupManifestUuidTask(project, output)
            this.uploadProguardTask = setupMappingFileUpload(project, variant, output)
            this.uploadNdkTask = setupNdkMappingFileUpload(project, variant, output)
        }
    }

    private void setupSplitsDiscovery(Project project, BaseVariant variant) {
        def task = project.tasks.findByName("splitsDiscoveryTask${taskNameForVariant(variant)}")
        task.doLast {
            project.ext.splitsInfo = new SplitsInfo()
            project.ext.splitsInfo.densityFilters = task.densityFilters
            project.ext.splitsInfo.languageFilters = task.languageFilters
            project.ext.splitsInfo.abiFilters = task.abiFilters
        }
    }

    /**
     * Creates a bugsnag task to upload proguard mapping file
     */
    private BugsnagUploadProguardTask setupMappingFileUpload(Project project, BaseVariant variant, BaseVariantOutput output) {
        def uploadTask = project.tasks.create("uploadBugsnag${taskNameForOutput(output)}Mapping", BugsnagUploadProguardTask)
        uploadTask.partName = isJackEnabled(project, variant) ? "jack" : "proguard"
        prepareUploadTask(uploadTask, output, variant, project)
        uploadTask
    }

    private BugsnagUploadNdkTask setupNdkMappingFileUpload(Project project, BaseVariant variant, BaseVariantOutput output) {
        File symbolPath = getSymbolPath(output)
        File intermediatePath = getIntermediatePath(symbolPath)
        BugsnagUploadNdkTask uploadNdkTask = null

        if (project.bugsnag.ndk) {
            // Create a Bugsnag task to upload NDK mapping file(s)
            uploadNdkTask = project.tasks.create("uploadBugsnagNdk${taskNameForOutput(output)}Mapping", BugsnagUploadNdkTask)
            prepareUploadTask(uploadNdkTask, output, variant, project)

            uploadNdkTask.intermediatePath = intermediatePath
            uploadNdkTask.symbolPath = symbolPath
            uploadNdkTask.variantName = taskNameForVariant(variant)
            uploadNdkTask.projectDir = project.projectDir
            uploadNdkTask.rootDir = project.rootDir
            uploadNdkTask.toolchain = getCmakeToolchain(project, variant)
            uploadNdkTask.sharedObjectPath = project.bugsnag.sharedObjectPath
        }
        uploadNdkTask
    }

    private static void prepareUploadTask(uploadTask, BaseVariantOutput output, BaseVariant variant, Project project) {
        uploadTask.group = GROUP_NAME
        uploadTask.variantOutput = output
        uploadTask.variant = variant
        uploadTask.applicationId = variant.applicationId
        uploadTask.mustRunAfter output.assemble

        if (project.bugsnag.autoUpload) {
            project.tasks.findByName("package${taskNameForVariant(variant)}").finalizedBy {
                uploadTask
            }
        }
    }

    private BugsnagManifestTask setupManifestUuidTask(Project project, BaseVariantOutput output) {
        project.logger.debug("Adding Build UUID to manifest")

        BugsnagManifestTask manifestTask = project.tasks.create("processBugsnag${taskNameForOutput(output)}Manifest", BugsnagManifestTask)
        manifestTask.variantOutput = output
        manifestTask.group = GROUP_NAME
        manifestTask.mustRunAfter output.processManifest
//        manifestTask.onlyIf { it.shouldRun() }

        output.packageApplication.dependsOn manifestTask
        return manifestTask
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
    private static BugsnagProguardConfigTask setupProguardAutoConfig(Project project, BaseVariant variant) {
        BugsnagProguardConfigTask proguardConfigTask = project.tasks.create("processBugsnag${taskNameForVariant(variant)}Proguard", BugsnagProguardConfigTask)
        proguardConfigTask.group = GROUP_NAME
        proguardConfigTask.variant = variant

        if (project.bugsnag.autoProguardConfig) {
            project.logger.debug("Bugsnag autoproguard config enabled")
            variant.packageApplication.dependsOn proguardConfigTask
        }
        return proguardConfigTask
    }

    private static String taskNameForVariant(BaseVariant variant) {
        variant.name.capitalize()
    }

    private static String taskNameForOutput(BaseVariantOutput output) {
        output.name.capitalize()
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
