package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.GradleVersions
import com.bugsnag.android.gradle.internal.outputZipFile
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import com.bugsnag.android.gradle.internal.versionNumber
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
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
sealed class BugsnagGenerateProguardTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates a compressed JVM mapping file for upload to Bugsnag"
    }

    @get:InputFiles
    abstract val mappingFileProperty: ConfigurableFileCollection

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
            return when {
                project.gradle.versionNumber() >= GradleVersions.VERSION_5_3 -> {
                    project.tasks.register<BugsnagGenerateProguardTaskGradle53Plus>(name, configurationAction)
                }
                else -> {
                    project.tasks.register<BugsnagGenerateProguardTaskLegacy>(name, configurationAction)
                }
            }
        }
    }
}

/**
 * Legacy [BugsnagGenerateProguardTask] task that requires using [getProject] and
 * [ProjectLayout.configurableFiles].
 */
internal open class BugsnagGenerateProguardTaskLegacy @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : BugsnagGenerateProguardTask(objects) {

    @get:InputFiles
    override val mappingFileProperty: ConfigurableFileCollection = projectLayout.configurableFiles()
}

internal open class BugsnagGenerateProguardTaskGradle53Plus @Inject constructor(
    objects: ObjectFactory
) : BugsnagGenerateProguardTask(objects) {

    @get:InputFiles
    override val mappingFileProperty: ConfigurableFileCollection = objects.fileCollection()
}
