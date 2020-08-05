package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class BugsnagInstallJniLibsTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    init {
        description = "Copies shared object files from the bugsnag-android AAR to the required build directory"
        group = BugsnagPlugin.GROUP_NAME
    }

    @get:OutputDirectory
    val buildDirDestination: RegularFileProperty = objects.fileProperty()

    @get:InputFiles
    val bugsnagArtefacts: Property<FileCollection> = objects.property(FileCollection::class.java)

    /**
     * Looks at all the dependencies and their dependencies and finds the `com.bugsnag` artifacts with SO files.
     */
    @TaskAction
    fun setupNdkProject() {
        bugsnagArtefacts.get().forEach { file: File ->
            project.copy {
                it.from(project.zipTree(file))
                it.into(project.file(buildDirDestination))
            }
        }
    }

    companion object {
        private val sharedObjectAarIds = listOf("bugsnag-android", "bugsnag-android-ndk",
            "bugsnag-plugin-android-anr", "bugsnag-plugin-android-ndk")

        internal fun resolveBugsnagArtefacts(project: Project): FileCollection {
            val files = project.configurations
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
            return project.files(files)
        }
    }
}
