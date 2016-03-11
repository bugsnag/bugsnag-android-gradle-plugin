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
                def manifestPath = variant.outputs.first().processManifest.manifestOutputFile

                // Create a Bugsnag task to add a build UUID to AndroidManifest.xml
                BugsnagManifestTask manifestTask = project.tasks.create("process${variant.name.capitalize()}BugsnagManifest", BugsnagManifestTask)
                manifestTask.group = GROUP_NAME
                manifestTask.manifestPath = manifestPath
                manifestTask.dependsOn variant.outputs.first().processManifest

                // Create a Bugsnag task to upload proguard mapping file
                BugsnagUploadTask uploadTask = project.tasks.create("uploadBugsnag${variant.name.capitalize()}Mapping", BugsnagUploadTask)
                uploadTask.group = GROUP_NAME
                uploadTask.manifestPath = manifestPath
                uploadTask.applicationId = variant.applicationId
                uploadTask.mappingFile = variant.getMappingFile()

                // Automatically add UUID to manifest during a build
                variant.outputs.first().processResources.dependsOn manifestTask

                // Automatically upload proguard mappings to Bugsnag during a build
                if(project.tasks.findByPath("proguard${variantName}")) {
                    uploadTask.dependsOn project.tasks["proguard${variantName}"]
                }
            }
        }
    }
}
