package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class BugsnagNdkSetupTask extends DefaultTask {

    BugsnagNdkSetupTask() {
        super()
        this.description = "Copies shared object files from the bugsnag-android AAR to the required build directory"
    }

    @TaskAction
    void setupNdkProject() {
        def configs = project.configurations.findAll {
            it.toString().contains('CompileClasspath')
        }.each { config ->
            def artifact = config.resolvedConfiguration.getResolvedArtifacts().find {
                def identifier = it.getId().getComponentIdentifier().toString()
                identifier.contains("bugsnag-android-ndk") && it.getFile() != null
            }
            if (artifact) {
                def artifactFile = artifact.getFile()
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


