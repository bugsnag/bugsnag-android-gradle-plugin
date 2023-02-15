package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariantOutput
import com.bugsnag.android.gradle.internal.VariantTaskCompanion
import com.bugsnag.android.gradle.internal.dependsOn
import com.bugsnag.android.gradle.internal.forBuildOutput
import com.bugsnag.android.gradle.internal.intermediateForGenerateJvmMapping
import com.bugsnag.android.gradle.internal.outputZipFile
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

/**
 * Task to generate compressed JVM mapping files to Bugsnag.
 */
open class BugsnagGenerateProguardTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates a compressed JVM mapping file for upload to Bugsnag"
    }

    @get:InputFile
    override val manifestInfo: RegularFileProperty = objects.fileProperty()

    @get:InputFiles
    val mappingFileProperty: ConfigurableFileCollection = objects.fileCollection()

    @get:OutputFile
    val archiveOutputFile: RegularFileProperty = objects.fileProperty()

    @get:Input
    val failOnUploadError: Property<Boolean> = objects.property()

    @TaskAction
    fun upload() {
        val mappingFile = resolveMappingFile()
        if (mappingFile.length() == 0L) { // proguard's -dontobfuscate generates an empty mapping file
            logger.warn("Bugsnag: Ignoring empty proguard file")
            return
        }
        val archive = archiveOutputFile.asFile.get()
        mappingFile.inputStream().use { stream ->
            outputZipFile(stream, archive)
        }
    }

    private fun resolveMappingFile(): File {
        val mappingFile = mappingFileProperty.filter(File::exists).singleFile
        if (!mappingFile.exists()) {
            logger.warn("Bugsnag: Mapping file not found: $mappingFile")
            check(failOnUploadError.get()) { "Mapping file not found: $mappingFile" }
        }
        return mappingFile
    }

    companion object : VariantTaskCompanion<BugsnagGenerateProguardTask> {
        override fun taskNameFor(variantOutputName: String) = "generateBugsnag${variantOutputName.capitalize()}Mapping"

        fun archiveOutputFile(project: Project, output: BaseVariantOutput): Provider<RegularFile> =
            forBuildOutput(project, output).flatMap { it.archiveOutputFile }

        fun register(
            project: Project,
            output: BaseVariantOutput,
            failOnUploadError: Property<Boolean>,
            mappingFilesProvider: Provider<FileCollection>
        ) = register(project, output) {
            this.manifestInfo.set(BugsnagManifestUuidTask.manifestInfoForOutput(project, output))
            this.archiveOutputFile.set(intermediateForGenerateJvmMapping(project, output))
            this.failOnUploadError.set(failOnUploadError)
            this.mappingFileProperty.from(mappingFilesProvider)
        }.dependsOn(BugsnagManifestUuidTask.forBuildOutput(project, output))
    }
}
