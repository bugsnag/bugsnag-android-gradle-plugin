package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariantOutput
import com.bugsnag.android.gradle.SharedObjectMappingFileFactory.NDK_SO_MAPPING_DIR
import com.bugsnag.android.gradle.internal.GradleVersions
import com.bugsnag.android.gradle.internal.includesAbi
import com.bugsnag.android.gradle.internal.mapProperty
import com.bugsnag.android.gradle.internal.register
import com.bugsnag.android.gradle.internal.versionNumber
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import javax.inject.Inject

/**
 * Task that generates NDK shared object mapping files for upload to Bugsnag.
 */
sealed class BugsnagGenerateNdkSoMappingTask(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates NDK mapping files for upload to Bugsnag"
    }

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:Internal
    internal lateinit var variantOutput: ApkVariantOutput

    @get:OutputDirectory
    val intermediateOutputDir: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir(NDK_SO_MAPPING_DIR))

    @get:Input
    val objDumpPaths: MapProperty<String, String> = objects.mapProperty()

    @get:InputFiles
    abstract val searchDirectories: ConfigurableFileCollection

    @TaskAction
    fun generateMappingFiles() {
        logger.lifecycle("Generating NDK mapping files")
        val searchDirs = searchDirectories.files.toList()
        val files = findSharedObjectMappingFiles(searchDirs)
        processFiles(files)
    }

    private fun findSharedObjectMappingFiles(
        searchDirectories: List<File>
    ): Collection<File> {
        return searchDirectories.flatMap(this::findSharedObjectFiles)
            .toSortedSet(compareBy { it.absolutePath })
    }

    /**
     * Searches the subdirectories of a given path for SO files. These are added to a
     * collection and returned if they should be uploaded by the current task.
     *
     * If the variantOutput is an APK split the splitArch parameter should be non-null,
     * as this allows the avoidance of unnecessary uploads of all architectures for each split.
     *
     * @param searchDirectory The parent path to search. Each subdirectory should
     * represent an architecture
     */
    private fun findSharedObjectFiles(searchDirectory: File): Collection<File> {
        return if (searchDirectory.exists() && searchDirectory.isDirectory) {
            searchDirectory.walkTopDown()
                .onEnter { archDir -> variantOutput.includesAbi(archDir.name) }
                .filter { file -> file.extension == "so" }
                .toSet()
        } else {
            emptySet()
        }
    }

    private fun processFiles(files: Collection<File>) {
        logger.info("Bugsnag: Found shared object files for upload: $files")

        files.forEach { sharedObjectFile ->
            val arch = sharedObjectFile.parentFile.name
            val params = SharedObjectMappingFileFactory.Params(
                sharedObjectFile,
                requireNotNull(Abi.findByName(arch)),
                objDumpPaths.get(),
                intermediateOutputDir.get().asFile
            )
            val outputFile = SharedObjectMappingFileFactory.generateSoMappingFile(project, params)
            if (outputFile != null) {
                logger.info("Bugsnag: Created symbol file for $arch at $outputFile")
            }
        }
    }

    companion object {
        internal fun register(
            project: Project,
            name: String,
            configurationAction: BugsnagGenerateNdkSoMappingTask.() -> Unit
        ): TaskProvider<out BugsnagGenerateNdkSoMappingTask> {
            val gradleVersion = project.gradle.versionNumber()
            return when {
                gradleVersion >= GradleVersions.VERSION_5_3 -> {
                    project.tasks.register<BugsnagGenerateNdkSoMappingTask53Plus>(name, configurationAction)
                }
                else -> {
                    project.tasks.register<BugsnagGenerateNdkSoMappingTaskLegacy>(name, configurationAction)
                }
            }
        }
    }
}

/** A legacy [BugsnagGenerateNdkSoMappingTask] that uses [ProjectLayout.configurableFiles]. */
internal open class BugsnagGenerateNdkSoMappingTaskLegacy @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : BugsnagGenerateNdkSoMappingTask(objects, projectLayout) {
    @Suppress("DEPRECATION") // Here for backward compat
    @get:InputFiles
    override val searchDirectories: ConfigurableFileCollection = projectLayout.configurableFiles()
}

internal open class BugsnagGenerateNdkSoMappingTask53Plus @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : BugsnagGenerateNdkSoMappingTask(objects, projectLayout) {
    @get:InputFiles
    override val searchDirectories: ConfigurableFileCollection = objects.fileCollection()
}
