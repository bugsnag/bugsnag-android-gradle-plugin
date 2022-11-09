package com.bugsnag.android.gradle

import com.android.build.VariantOutput
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.BaseVariant
import com.bugsnag.android.gradle.internal.AbstractSoMappingTask
import com.bugsnag.android.gradle.internal.ExternalNativeBuildTaskUtil
import com.bugsnag.android.gradle.internal.NdkToolchain
import com.bugsnag.android.gradle.internal.VariantTaskCompanion
import com.bugsnag.android.gradle.internal.clearDir
import com.bugsnag.android.gradle.internal.dependsOn
import com.bugsnag.android.gradle.internal.forBuildOutput
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import javax.inject.Inject

/**
 * Task that generates NDK shared object mapping files for upload to Bugsnag.
 */
internal abstract class BugsnagGenerateNdkSoMappingTask @Inject constructor(
    objects: ObjectFactory
) : AbstractSoMappingTask() {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates NDK mapping files for upload to Bugsnag"
    }

    @get:Input
    @get:Optional
    val abi: Property<String> = objects.property()

    @get:InputFiles
    val searchDirectories: ConfigurableFileCollection = objects.fileCollection()

    @TaskAction
    fun generateMappingFiles() {
        logger.lifecycle("Generating NDK mapping files")
        val files = findSharedObjectMappingFiles()
        processFiles(files)
    }

    private fun findSharedObjectMappingFiles(): Collection<File> {
        return searchDirectories
            .flatMap(this::findSharedObjectFiles)
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
        outputDirectory.get().asFile.clearDir()

        files.forEach { sharedObjectFile ->
            val arch = sharedObjectFile.parentFile.name
            val abi = requireNotNull(Abi.findByName(arch)) { "unknown abi: $arch" }
            val outputFile = generateMappingFile(sharedObjectFile, abi)
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
            ndk: NdkToolchain,
            searchPaths: List<File>,
            soMappingOutputPath: String
        ): TaskProvider<out BugsnagGenerateNdkSoMappingTask> {
            val task = register(project, output) {
                abi.set(output.getFilter(VariantOutput.FilterType.ABI))
                ndkToolchain.set(ndk)

                val externalNativeBuildTaskUtil = ExternalNativeBuildTaskUtil(project.providers)

                searchDirectories.from(searchPaths)
                variant.externalNativeBuildProviders.forEach { provider ->
                    searchDirectories.from(externalNativeBuildTaskUtil.findSearchPaths(provider))
                }
                outputDirectory.set(project.layout.buildDirectory.dir(soMappingOutputPath))
            }

            task.dependsOn(BugsnagManifestUuidTask.forBuildOutput(project, output))
            variant.externalNativeBuildProviders.forEach { task.dependsOn(it) }

            return task
        }

        override fun taskNameFor(variantOutputName: String) =
            "generateBugsnagNdk${variantOutputName.capitalize()}Mapping"
    }
}
