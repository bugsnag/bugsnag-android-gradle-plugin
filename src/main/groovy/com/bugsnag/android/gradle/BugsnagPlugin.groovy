package com.bugsnag.android.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant

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

            // Create tasks for each variant
            project.android.applicationVariants.all { ApplicationVariant variant ->
                def variantName = variant.name.capitalize()
                def variantOutput = variant.outputs.first()
                def manifestPath = variantOutput.processManifest.manifestOutputFile

                // Create a Bugsnag task to add a "Bugsnag recommended proguard settings" file
                BugsnagProguardConfigTask proguardConfigTask = project.tasks.create("processBugsnag${variant.name.capitalize()}Proguard", BugsnagProguardConfigTask)
                proguardConfigTask.group = GROUP_NAME
                proguardConfigTask.applicationVariant = variant
                proguardConfigTask.dependsOn variantOutput.processManifest

                // Create a Bugsnag task to add a build UUID to AndroidManifest.xml
                BugsnagManifestTask manifestTask = project.tasks.create("processBugsnag${variant.name.capitalize()}Manifest", BugsnagManifestTask)
                manifestTask.group = GROUP_NAME
                manifestTask.manifestPath = manifestPath
                manifestTask.dependsOn variantOutput.processManifest

                // Create a Bugsnag task to upload proguard mapping file
                BugsnagUploadTask uploadTask = project.tasks.create("uploadBugsnag${variant.name.capitalize()}Mapping", BugsnagUploadTask)
                uploadTask.group = GROUP_NAME
                uploadTask.manifestPath = manifestPath
                uploadTask.applicationId = variant.applicationId
                uploadTask.mappingFile = variant.getMappingFile()
                uploadTask.mustRunAfter variantOutput.packageApplication

                // Automatically add the manifest editing task to the build process
                variantOutput.processResources.dependsOn manifestTask

                // Automatically add the proguard settings task to the build process
                if(project.bugsnag.autoProguardConfig) {
                    variantOutput.processResources.dependsOn proguardConfigTask
                }

                // Automatically add the proguard mapping upload task to the build process
                if(project.bugsnag.autoUpload) {
                    variant.getAssemble().dependsOn uploadTask
                }
            }
        }
    }
}
