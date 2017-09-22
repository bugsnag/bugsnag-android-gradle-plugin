package com.bugsnag.android.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
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

    void apply(Project project) {
        project.extensions.create("bugsnag", BugsnagPluginExtension)

        project.afterEvaluate {
            // Make sure the android plugin has been applied first
            if (!project.plugins.hasPlugin(AppPlugin)) {
                throw new IllegalStateException('Must apply \'com.android.application\' first!')
            }

            // Create tasks for each Build Variant
            // https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Variants
            project.android.applicationVariants.all { ApplicationVariant variant ->
                if (hasDisabledBugsnag(variant)) {
                    return
                }

                variant.outputs.all { output ->
                    def usedOutput = variant.outputs.first()
                    setupProguardAutoConfig(project, variant, usedOutput)
                    setupManifestUuidTask(project, variant, usedOutput)
                    setupMappingFileUpload(project, variant, usedOutput)
                    setupNdkMappingFileUpload(project, variant, usedOutput)
                }
            }
        }
    }

    private static void setupMappingFileUpload(Project project, ApplicationVariant variant,  BaseVariantOutput output) {
        // The Android build system supports creating multiple APKs
        // per Build Variant (Variant Outputs):
        // https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide/apk-splits
        //
        // Variant Outputs share most tasks so we only need to attach
        // Bugsnag tasks to the first output.
        def variantName = variant.name.capitalize()

        // Create a Bugsnag task to upload proguard mapping file
        def uploadTaskClass = BugsnagUploadProguardTask
        def uploadTask = project.tasks.create("uploadBugsnag${ variantName}Mapping", uploadTaskClass)
        uploadTask.partName = isJackEnabled(project, variant) ? "jack" : "proguard"

        uploadTask.group = GROUP_NAME
        uploadTask.output = output
        uploadTask.variant = variant
        uploadTask.applicationId = variant.applicationId

        if (project.bugsnag.autoUpload) {
            project.tasks.findByName("package${variantName}").finalizedBy {uploadTask}
        }
    }

    private static void setupNdkMappingFileUpload(Project project, ApplicationVariant variant,  BaseVariantOutput output) {
        File symbolPath = getSymbolPath(output)
        File intermediatePath = getIntermediatePath(symbolPath)
        BugsnagUploadNdkTask uploadNdkTask
        def variantName = variant.name.capitalize()

        if (project.bugsnag.ndk) {
            // Create a Bugsnag task to upload NDK mapping file(s)
            uploadNdkTask = project.tasks.create("uploadBugsnagNdk${variantName}Mapping", BugsnagUploadNdkTask)
            uploadNdkTask.group = GROUP_NAME
            uploadNdkTask.output = output
            uploadNdkTask.variant = variant
            uploadNdkTask.applicationId = variant.applicationId
            uploadNdkTask.intermediatePath = intermediatePath
            uploadNdkTask.symbolPath = symbolPath
            uploadNdkTask.variantName = variantName
            uploadNdkTask.projectDir = project.projectDir
            uploadNdkTask.rootDir = project.rootDir
            uploadNdkTask.toolchain = getCmakeToolchain(project, variant)
            uploadNdkTask.sharedObjectPath = project.bugsnag.sharedObjectPath

            if (project.bugsnag.autoUpload) {
                project.tasks.findByName("package${variantName}").finalizedBy {uploadNdkTask}
            }
        }
    }


    private static void setupManifestUuidTask(Project project, ApplicationVariant variant,  BaseVariantOutput output) {
        project.logger.debug("Adding Build UUID to manifest")

        BugsnagManifestTask manifestTask = project.tasks.create("processBugsnag${variant.name.capitalize()}Manifest", BugsnagManifestTask)
        manifestTask.output = output
        manifestTask.group = GROUP_NAME
        manifestTask.mustRunAfter output.processManifest
        manifestTask.onlyIf { it.shouldRun() }

        output.packageApplication.dependsOn manifestTask
    }

    private static void setupProguardAutoConfig(Project project, ApplicationVariant variant,  BaseVariantOutput output) {
        BugsnagProguardConfigTask proguardConfigTask = project.tasks.create("processBugsnag${variant.name.capitalize()}Proguard", BugsnagProguardConfigTask)
        proguardConfigTask.group = GROUP_NAME
        proguardConfigTask.applicationVariant = variant

        // Automatically add the "edit proguard settings" task to the
        // build process.
        //
        // This task must be called before ProGuard is run, but since
        // the name of the ProGuard task changed between 1.0 and 1.5
        // of the Android build tools, we'll hook into the "package"
        // task as a dependency, since this is always run before
        // ProGuard.
        //
        // For reference, in Android Build Tools 1.0, the ProGuard
        // task was named `proguardRelease`, and in 1.5+ the ProGuard
        // task is named `transformClassesAndResourcesWithProguardForRelease`
        // as it is now part of the "transforms" process.
        if (project.bugsnag.autoProguardConfig) {
            project.logger.debug("Bugsnag autoproguard config enabled")
            output.packageApplication.dependsOn proguardConfigTask
        }
    }

    /**
     * Create a Bugsnag task to add a "Bugsnag recommended proguard settings" file
     * @param variantName
     * @param variant
     * @return
     */

    /**
     *
     * Create a Bugsnag task to add a build UUID to AndroidManifest.xml
     * This task must be called after "process${variantName}Manifest", since it
     * requires that an AndroidManifest.xml exists in `build/intermediates`.
     *
     * @param variantName
     * @param manifestPath
     * @param variantOutput
     * @return
     */

    private static boolean hasDisabledBugsnag(ApplicationVariant variant) {
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
    private static boolean isJackEnabled(Project project, ApplicationVariant variant) {

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
    private static String getCmakeToolchain(Project project, ApplicationVariant variant) {

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
