package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.TaskAction

class BugsnagNdkSetupTask extends DefaultTask {

    BugsnagNdkSetupTask() {
        super()
        this.description = "Copies shared object files from the bugsnag-android AAR to the required build directory"
    }

    @TaskAction
    void setupNdkProject() {
        project.configurations.findAll {
            it.toString().contains('CompileClasspath')
        }.each { config ->
            ResolvedArtifact artifact = config.resolvedConfiguration.resolvedArtifacts.find {
                String identifier = it.id.componentIdentifier.toString()
                identifier.contains("bugsnag-android") && it.file != null
            }
            if (artifact) {
                File artifactFile = artifact.file
                File buildDir = project.buildDir
                File dst = new File(buildDir, "/intermediates/bugsnag-libs")

                project.copy {
                    from project.zipTree(artifactFile)
                    into(project.file(dst))
                }
            }
        }
    }
}
