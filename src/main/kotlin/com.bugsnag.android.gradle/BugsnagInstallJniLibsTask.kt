package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.stream.Collectors

open class BugsnagInstallJniLibsTask : DefaultTask() {

    init {
        description = "Copies shared object files from the bugsnag-android AAR to the required build directory"
        group = BugsnagPlugin.GROUP_NAME
    }

    @TaskAction
    fun setupNdkProject() {
        val artifacts = HashSet<ResolvedArtifact>()

        findCompileConfigurations().forEach { config ->
            config.firstLevelModuleDependencies.stream()
                .filter { (it.moduleGroup == "com.bugsnag") }
                .forEach {
                    artifacts.addAll(resolveArtifacts(it))
                }
        }
        artifacts.forEach { copyArtifact(it) }
    }

    fun resolveArtifacts(dependency: ResolvedDependency): Set<ResolvedArtifact> {
        return dependency.allModuleArtifacts.filter {
            val identifier = it.id.componentIdentifier.toString()
            val soArtefacts = listOf("bugsnag-android", "bugsnag-android-ndk",
                "bugsnag-plugin-android-anr", "bugsnag-plugin-android-ndk")

            val isBugsnagArtefact = soArtefacts.stream().anyMatch {
                identifier.contains(it)
            }
            isBugsnagArtefact
        }.toSet()
    }

    fun copyArtifact(artifact: ResolvedArtifact) {
        val artifactFile = artifact.file
        val buildDir = project.buildDir
        val dst = File(buildDir, "/intermediates/bugsnag-libs")

        project.copy {
            it.from(project.zipTree(artifactFile))
            it.into(project.file(dst))
        }
    }

    /**
     * @return the ResolvedConfiguration for any gradle configurations which add compile-time dependencies.
     * e.g. if 'bugsnag-android' is added as a dependency, it will be part of the 'api' configuration.
     */
    private fun findCompileConfigurations(): Set<ResolvedConfiguration> {
        return project.configurations
            .filter { it.toString().contains("CompileClasspath") }
            .stream()
            .map { it.resolvedConfiguration }
            .collect(Collectors.toSet())
    }
}
