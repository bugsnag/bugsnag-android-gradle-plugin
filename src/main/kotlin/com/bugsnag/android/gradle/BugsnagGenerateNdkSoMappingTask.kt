package com.bugsnag.android.gradle

import com.android.build.VariantOutput
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.BaseVariant
import com.bugsnag.android.gradle.internal.ExternalNativeBuildTaskUtil
import com.bugsnag.android.gradle.internal.VariantTaskCompanion
import com.bugsnag.android.gradle.internal.clearDir
import com.bugsnag.android.gradle.internal.dependsOn
import com.bugsnag.android.gradle.internal.forBuildOutput
import com.bugsnag.android.gradle.internal.mapProperty
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

/**
 * Task that generates NDK shared object mapping files for upload to Bugsnag.
 */
open class BugsnagGenerateNdkSoMappingTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates NDK mapping files for upload to Bugsnag"
    }

    @get:InputFile
    override val manifestInfo: RegularFileProperty = objects.fileProperty()

    @get:Input
    @get:Optional
    val abi: Property<String> = objects.property()

    @get:OutputDirectory
    val intermediateOutputDir: DirectoryProperty = objects.directoryProperty()

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
                .onEnter { archDir -> abi.getOrElse(archDir.name) == archDir.name }
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

    companion object : VariantTaskCompanion<BugsnagGenerateNdkSoMappingTask> {

        /**
         * SO files which should be ignored by the NDK upload task. These are Unity
         * library SO files and are handled by the Unity upload task.
         */
        internal val IGNORED_SO_FILES = hashSetOf("libunity.so", "libil2cpp.so", "libmain.so")

        fun register(
            project: Project,
            variant: BaseVariant,
            output: ApkVariantOutput,
            objdumpPaths: Provider<Map<String, String>>,
            searchPaths: List<File>,
            soMappingOutputPath: String
        ) = register(project, output) {
            abi.set(output.getFilter(VariantOutput.FilterType.ABI))
            objDumpPaths.set(objdumpPaths)
            manifestInfo.set(BugsnagManifestUuidTask.manifestInfoForOutput(project, output))

            val externalNativeBuildTaskUtil = ExternalNativeBuildTaskUtil(project.providers)

            searchDirectories.from(searchPaths)
            variant.externalNativeBuildProviders.forEach { provider ->
                searchDirectories.from(externalNativeBuildTaskUtil.findSearchPaths(provider))
            }
            intermediateOutputDir.set(project.layout.buildDirectory.dir(soMappingOutputPath))
        }.dependsOn(BugsnagManifestUuidTask.forBuildOutput(project, output))

        override fun taskNameFor(variantOutputName: String) =
            "generateBugsnagNdk${variantOutputName.capitalize()}Mapping"
    }
}
