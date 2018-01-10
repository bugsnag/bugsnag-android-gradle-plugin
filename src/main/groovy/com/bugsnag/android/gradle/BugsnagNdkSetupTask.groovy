package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class BugsnagNdkSetupTask extends DefaultTask {

    BugsnagNdkSetupTask() {
        super()
        this.description = "Assembles information about the build that will be sent to the releases API"
    }

    @TaskAction
    void setupNdkProject() {
        def configs = project.configurations.findAll {
            it.toString().contains('CompileClasspath')
        }.each { config ->
            def artifactFile = config.resolvedConfiguration.getFiles().find {
                it.toString().contains("bugsnag-android-ndk")
            }
            if (artifactFile) {
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


