package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariantOutput
import com.bugsnag.android.gradle.internal.NDK_SO_MAPPING_DIR
import com.bugsnag.android.gradle.internal.clearDir
import com.bugsnag.android.gradle.internal.includesAbi
import com.bugsnag.android.gradle.internal.mapProperty
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
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
open class BugsnagGenerateNdkSoMappingTask @Inject constructor(
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

    @get:Optional
    @get:Input
    override val versionCode: Property<Int> = objects.property()

    @get:Internal
    internal lateinit var variantOutput: ApkVariantOutput

    @get:OutputDirectory
    val intermediateOutputDir: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir(NDK_SO_MAPPING_DIR))

    @get:Input
    val objDumpPaths: MapProperty<String, String> = objects.mapProperty()

    @get:InputFiles
    val searchDirectories: ConfigurableFileCollection = objects.fileCollection()

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
                .filter { !IGNORED_SO_FILES.contains(it.name) }
                .toSet()
        } else {
            emptySet()
        }
    }

    private fun processFiles(files: Collection<File>) {
        logger.info("Bugsnag: Found shared object files for upload: $files")
        val outputDir = intermediateOutputDir.get().asFile
        outputDir.clearDir()

        files.forEach { sharedObjectFile ->
            val arch = sharedObjectFile.parentFile.name
            val params = SharedObjectMappingFileFactory.Params(
                sharedObjectFile,
                requireNotNull(Abi.findByName(arch)),
                objDumpPaths.get(),
                outputDir
            )
            val outputFile = SharedObjectMappingFileFactory.generateSoMappingFile(project, params)
            if (outputFile != null) {
                logger.info("Bugsnag: Created symbol file for $arch at $outputFile")
            }
        }
    }

    companion object {

        /**
         * SO files which should be ignored by the NDK upload task. These are Unity
         * library SO files and are handled by the Unity upload task.
         */
        internal val IGNORED_SO_FILES = listOf("libunity.so", "libil2cpp.so", "libmain.so")

        internal fun register(
            project: Project,
            name: String,
            configurationAction: BugsnagGenerateNdkSoMappingTask.() -> Unit
        ): TaskProvider<out BugsnagGenerateNdkSoMappingTask> {
            return project.tasks.register(name, configurationAction)
        }
    }
}
