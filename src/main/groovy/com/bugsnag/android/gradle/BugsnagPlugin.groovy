package com.bugsnag.android.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant

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
            if(!project.plugins.hasPlugin(AppPlugin)) {
                throw new IllegalStateException('Must apply \'com.android.application\' first!')
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
                //
                // Variant Outputs share most tasks so we only need to attach
                // Bugsnag tasks to the first output.
                def variantOutput = variant.outputs.first()
                def variantName = variant.name.capitalize()

                // The location of the "intermediate" AndroidManifest.xml
                def manifestPath = variantOutput.processManifest.manifestOutputFile

                // Create a Bugsnag task to add a "Bugsnag recommended proguard settings" file
                BugsnagProguardConfigTask proguardConfigTask = project.tasks.create("processBugsnag${variantName}Proguard", BugsnagProguardConfigTask)
                proguardConfigTask.group = GROUP_NAME
                proguardConfigTask.applicationVariant = variant

                // Create a Bugsnag task to add a build UUID to AndroidManifest.xml
                // This task must be called after "process${variantName}Manifest", since it
                // requires that an AndroidManifest.xml exists in `build/intermediates`.
                BugsnagManifestTask manifestTask = project.tasks.create("processBugsnag${variantName}Manifest", BugsnagManifestTask)
                manifestTask.group = GROUP_NAME
                manifestTask.manifestPath = manifestPath
                manifestTask.mustRunAfter variantOutput.processManifest

                // Create a Bugsnag task to upload proguard mapping file
                BugsnagUploadTask uploadTask = project.tasks.create("uploadBugsnag${variantName}Mapping", BugsnagUploadTask)
                uploadTask.group = GROUP_NAME
                uploadTask.manifestPath = manifestPath
                uploadTask.applicationId = variant.applicationId
                uploadTask.mappingFile = variant.getMappingFile()
                uploadTask.mustRunAfter variantOutput.packageApplication

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
                if(project.bugsnag.autoProguardConfig) {
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
                if(project.bugsnag.autoUpload) {
                    variant.getAssemble().dependsOn uploadTask
                }
            }
        }
    }
}
