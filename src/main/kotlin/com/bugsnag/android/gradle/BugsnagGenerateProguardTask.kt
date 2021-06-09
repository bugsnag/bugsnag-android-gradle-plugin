package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.outputZipFile
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
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

    @get:InputFiles
    val mappingFileProperty: ConfigurableFileCollection = objects.fileCollection()

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:Optional
    @get:Input
    override val versionCode: Property<Int> = objects.property()

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
            if (failOnUploadError.get()) {
                throw IllegalStateException("Mapping file not found: $mappingFile")
            }
        }
        return mappingFile
    }

    companion object {

        /**
         * Registers the appropriate subtype to this [project] with the given [name] and
         * [configurationAction]
         */
        internal fun register(
            project: Project,
            name: String,
            configurationAction: BugsnagGenerateProguardTask.() -> Unit
        ): TaskProvider<out BugsnagGenerateProguardTask> {
            return project.tasks.register(name, configurationAction)
        }
    }
}
