package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class BugsnagInstallJniLibsTask : DefaultTask() {

    init {
        description = "Copies shared object files from the bugsnag-android AAR to the required build directory"
        group = BugsnagPlugin.GROUP_NAME
    }

    private val sharedObjectAarIds = listOf("bugsnag-android", "bugsnag-android-ndk",
        "bugsnag-plugin-android-anr", "bugsnag-plugin-android-ndk")

    @get:OutputDirectory
    var buildDirDestination = File(project.buildDir, "/intermediates/bugsnag-libs")

    @get:InputFiles
    var bugsnagArtefacts = resolveBugsnagArtefacts()

    /**
     * Looks at all the dependencies and their dependencies and finds the `com.bugsnag` artifacts with SO files.
     */
    @TaskAction
    fun setupNdkProject() {
        bugsnagArtefacts.forEach { file: File ->
            project.copy {
                it.from(project.zipTree(file))
                it.into(project.file(buildDirDestination))
            }
        }
    }

    private fun resolveBugsnagArtefacts(): Set<File> {
        return project.configurations
            .filter { it.toString().contains("CompileClasspath") }
            .map { it.resolvedConfiguration }
            .flatMap { it.firstLevelModuleDependencies }
            .filter { it.moduleGroup == "com.bugsnag" }
            .flatMap { it.allModuleArtifacts }
            .filter {
                val identifier = it.id.componentIdentifier.toString()
                sharedObjectAarIds.any { bugsnagId -> identifier.contains(bugsnagId) }
            }
            .map { it.file }
            .toSet()
    }
}
