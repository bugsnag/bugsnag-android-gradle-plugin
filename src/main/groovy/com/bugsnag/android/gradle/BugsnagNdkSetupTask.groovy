package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.TaskAction

import java.util.stream.Collectors

class BugsnagNdkSetupTask extends DefaultTask {

    BugsnagNdkSetupTask() {
        super()
        this.description = "Copies shared object files from the bugsnag-android AAR to the required build directory"
    }

    @TaskAction
    void setupNdkProject() {
        Set<ResolvedArtifact> artifacts = new HashSet<>()

        findCompileConfigurations().each { config ->
            config.firstLevelModuleDependencies.stream()
                .filter { (it.moduleGroup == "com.bugsnag") }
                .forEach {
                    artifacts.addAll(resolveArtifacts(it))
                }
        }
        artifacts.forEach { copyArtifact(it) }
    }

    Set<ResolvedArtifact> resolveArtifacts(ResolvedDependency dependency) {
        dependency.allModuleArtifacts.findAll {
            String identifier = it.id.componentIdentifier.toString()
            List<String> soArtefacts = ["bugsnag-android", "bugsnag-android-ndk",
                                        "bugsnag-plugin-android-anr", "bugsnag-plugin-android-ndk",]

            boolean isBugsnagArtefact = soArtefacts.stream().anyMatch {
                identifier.contains(it)
            }
            isBugsnagArtefact && it.file != null
        }
    }

    void copyArtifact(ResolvedArtifact artifact) {
        File artifactFile = artifact.file
        File buildDir = project.buildDir
        File dst = new File(buildDir, "/intermediates/bugsnag-libs")

        project.copy {
            from project.zipTree(artifactFile)
            into(project.file(dst))
        }
    }

    /**
     * @return the ResolvedConfiguration for any gradle configurations which add compile-time dependencies.
     * e.g. if 'bugsnag-android' is added as a dependency, it will be part of the 'api' configuration.
     */
    private Set<ResolvedConfiguration> findCompileConfigurations() {
        project.configurations
            .findAll { it.toString().contains('CompileClasspath') }
            .stream()
            .map { it.resolvedConfiguration }
            .collect(Collectors.toSet())
    }

    /**
     * Copies artifacts to a location in the build directory where their shared object files can be read.
     */
}
