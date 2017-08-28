package com.bugsnag.android.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
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

            // Check the plugin is enabled, else dont even create tasks
            if (!project.bugsnag.enabled) {
                return
            }

            // Create tasks for each Build Variant
            // https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Variants
            project.android.applicationVariants.all { ApplicationVariant variant ->
                def hasDisabledBugsnag = {
                    it.ext.properties.containsKey("enableBugsnag") && !it.ext.enableBugsnag
                }

                // Ignore any conflicting properties, bail if anything has a disable flag.
                if ((variant.productFlavors + variant.buildType).any(hasDisabledBugsnag)) {
                    return
                }

                // The Android build system supports creating multiple APKs
                // per Build Variant (Variant Outputs):
                // https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide/apk-splits
                variant.outputs.each { variantOutput ->
                    // Retrieve the name of the variant output. This name is guaranteed to be unique
                    def outputName = variantOutput.name
                    // The location of the "intermediate" AndroidManifest.xml
                    def manifestPath = variantOutput.processManifest.manifestOutputFile

                    // Location where Proguard symbols are output
                    def symbolPath = variantOutput.processResources.textSymbolOutputDir
                    def intermediatePath = null;

                    if (symbolPath != null) {
                        intermediatePath = symbolPath.parentFile.parentFile
                    }

                    // Create a Bugsnag task to add a "Bugsnag recommended proguard settings" file
                    BugsnagProguardConfigTask proguardConfigTask = project.tasks.create("processBugsnag${outputName}Proguard", BugsnagProguardConfigTask)
                    proguardConfigTask.group = GROUP_NAME
                    proguardConfigTask.applicationVariant = variant

                    // Create a Bugsnag task to add a build UUID to AndroidManifest.xml
                    // This task must be called after "process${variantName}Manifest", since it
                    // requires that an AndroidManifest.xml exists in `build/intermediates`.
                    BugsnagManifestTask manifestTask = project.tasks.create("processBugsnag${outputName}Manifest", BugsnagManifestTask)
                    manifestTask.group = GROUP_NAME
                    manifestTask.manifestPath = manifestPath
                    manifestTask.mustRunAfter variantOutput.processManifest
                    manifestTask.onlyIf { it.shouldRun() }

                    // Create a Bugsnag task to upload proguard mapping file
                    def uploadTaskClass = isJackEnabled(project, variant) ? BugsnagUploadJackTask : BugsnagUploadProguardTask
                    def uploadTask = project.tasks.create("uploadBugsnag${outputName}Mapping", uploadTaskClass)
                    uploadTask.group = GROUP_NAME
                    uploadTask.manifestPath = manifestPath
                    uploadTask.applicationId = variant.applicationId
                    uploadTask.mappingFile = variant.getMappingFile()
                    uploadTask.mustRunAfter variantOutput.packageApplication

                    BugsnagUploadNdkTask uploadNdkTask
                    if (project.bugsnag.ndk) {
                        // Create a Bugsnag task to upload NDK mapping file(s)
                        uploadNdkTask = project.tasks.create("uploadBugsnagNdk${outputName}Mapping", BugsnagUploadNdkTask)
                        uploadNdkTask.group = GROUP_NAME
                        uploadNdkTask.manifestPath = manifestPath
                        uploadNdkTask.applicationId = variant.applicationId
                        uploadNdkTask.intermediatePath = intermediatePath
                        uploadNdkTask.symbolPath = symbolPath
                        uploadNdkTask.variantName = variant.name
                        uploadNdkTask.projectDir = project.projectDir
                        uploadNdkTask.rootDir = project.rootDir
                        uploadNdkTask.toolchain = getCmakeToolchain(project, variant)
                        uploadNdkTask.sharedObjectPath = project.bugsnag.sharedObjectPath
                        uploadNdkTask.mustRunAfter variantOutput.packageApplication
                    }

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
                        variantOutput.packageApplication.dependsOn proguardConfigTask
                    }

                    // Automatically add the "add build uuid to manifest" task to
                    // the build process.
                    //
                    // This task must be called after processManifest (see above),
                    // but before any source code is compiled, and before
                    // Bugsnag's upload task is run, since both require a UUID to
                    // be present in AndroidManifest.xml.
                    variantOutput.processResources.dependsOn manifestTask

                    // Automatically add the "upload proguard mappings" task to
                    // the build process.
                    //
                    // This task must be called after `packageApplication` (see
                    // above), but we also want this to run automatically as part
                    // of a typical build, so we hook into the `assemble` task.
                    if (project.bugsnag.autoUpload) {
                        variant.getAssemble().dependsOn uploadTask

                        if (project.bugsnag.ndk) {
                            variant.getAssemble().dependsOn uploadNdkTask
                        }
                    }
                }
            }
        }
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

            return project.android.defaultConfig.jackOptions.enabled;
        } else {
            return false;
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
                for (String arg : args ) {
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
        for (String arg : args ) {
            if (arg.startsWith("-DANDROID_TOOLCHAIN")) {
                return arg.substring(arg.indexOf("=") + 1).trim()
            }
        }

        return null;
    }

    /**
     * Finds the given build type in a TreeSet of buildtypes
     * @param set The TreeSet of build types
     * @param name The name of the buildtype to search for
     * @return The buildtype, or null if not found
     */
    private static BuildType findNode(TreeSet<BuildType> set, String name) {

        Iterator<BuildType> iterator = set.iterator();
        while(iterator.hasNext()) {
            BuildType node = iterator.next();
            if(node.getName().equals(name))
                return node;
        }

        return null;
    }
}
