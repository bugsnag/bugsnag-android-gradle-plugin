package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.WorkResult
import java.io.File
import javax.inject.Inject

sealed class BugsnagInstallJniLibsTask(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask() {
    init {
        description = "Copies shared object files from the bugsnag-android AAR to the required build directory"
        group = BugsnagPlugin.GROUP_NAME
    }

    @get:OutputDirectory
    val buildDirDestination: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir("intermediates/bugsnag-libs"))

    @get:InputFiles
    val bugsnagArtifacts: ConfigurableFileCollection = objects.fileCollection()

    abstract fun copy(action: (CopySpec) -> Unit): WorkResult

    /**
     * Looks at all the dependencies and their dependencies and finds the `com.bugsnag` artifacts with SO files.
     */
    @TaskAction
    fun setupNdkProject() {
        bugsnagArtifacts.forEach { file: File ->
            copy {
                it.from(project.zipTree(file))
                it.into(project.file(buildDirDestination))
            }
        }
    }

    companion object {
        private val sharedObjectAarIds = listOf("bugsnag-android", "bugsnag-android-ndk",
            "bugsnag-plugin-android-anr", "bugsnag-plugin-android-ndk")

        internal fun resolveBugsnagArtifacts(project: Project): FileCollection {
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

        /**
         * Registers the appropriate subtype to this [project] with the given [name] and
         * [configurationAction]
         */
        internal fun register(
            project: Project,
            name: String,
            configurationAction: BugsnagInstallJniLibsTask.() -> Unit
        ): TaskProvider<out BugsnagInstallJniLibsTask> {
            return if (project.gradle.gradleVersion.startsWith('6')) {
                project.tasks.register(name, BugsnagInstallJniLibsTaskGradle6Plus::class.java, configurationAction)
            } else  {
                project.tasks.register(name, BugsnagInstallJniLibsTaskLegacy::class.java, configurationAction)
            }
        }
    }
}

/** Legacy [BugsnagInstallJniLibsTask] task that requires using [getProject]. */
internal open class BugsnagInstallJniLibsTaskLegacy @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : BugsnagInstallJniLibsTask(objects, projectLayout) {
    override fun copy(action: (CopySpec) -> Unit): WorkResult {
        return project.copy(action)
    }
}

/**
 * A Gradle 6.0+ compatible [BugsnagInstallJniLibsTask], which uses [FileSystemOperations]
 * and supports configuration caching.
 */
internal open class BugsnagInstallJniLibsTaskGradle6Plus @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout,
    private val fsOperations: FileSystemOperations
) : BugsnagInstallJniLibsTask(objects, projectLayout) {
    override fun copy(action: (CopySpec) -> Unit): WorkResult = fsOperations.copy(action)
}
