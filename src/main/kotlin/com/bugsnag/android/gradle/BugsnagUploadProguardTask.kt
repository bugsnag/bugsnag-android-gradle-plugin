package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
import com.bugsnag.android.gradle.internal.GradleVersions
import com.bugsnag.android.gradle.internal.UploadRequestClient
import com.bugsnag.android.gradle.internal.md5HashCode
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import com.bugsnag.android.gradle.internal.versionNumber
import okhttp3.RequestBody.Companion.asRequestBody
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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

/**
 * Task to upload ProGuard mapping files to Bugsnag.
 *
 * Reads meta-data tags from the project's AndroidManifest.xml to extract a
 * build UUID (injected by BugsnagManifestTask) and a Bugsnag API Key:
 *
 * https://developer.android.com/guide/topics/manifest/manifest-intro.html
 * https://developer.android.com/guide/topics/manifest/meta-data-element.html
 *
 * This task must be called after ProGuard mapping files are generated, so
 * it is usually safe to have this be the absolute last task executed during
 * a build.
 */
sealed class BugsnagUploadProguardTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask(), AndroidManifestInfoReceiver, BugsnagFileUploadTask {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Uploads the mapping file to Bugsnag"
    }

    @get:Internal
    internal val uploadRequestClient: Property<UploadRequestClient> = objects.property()

    @get:Internal
    override val httpClientHelper: Property<BugsnagHttpClientHelper> = objects.property()

    @get:InputFiles
    abstract val mappingFileProperty: ConfigurableFileCollection

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val requestOutputFile: RegularFileProperty = objects.fileProperty()

    @get:Input
    override val failOnUploadError: Property<Boolean> = objects.property()

    @get:Input
    override val overwrite: Property<Boolean> = objects.property()

    @get:Input
    override val endpoint: Property<String> = objects.property()

    @get:Input
    override val retryCount: Property<Int> = objects.property()

    @get:Input
    override val timeoutMillis: Property<Long> = objects.property()

    @TaskAction
    fun upload() {
        val mappingFile = mappingFileProperty.singleFile
        if (mappingFile.length() == 0L) { // proguard's -dontobfuscate generates an empty mapping file
            logger.warn("Bugsnag: Ignoring empty proguard file")
            return
        }
        if (!mappingFile.exists()) {
            logger.warn("Bugsnag: Mapping file not found: $mappingFile")
            if (failOnUploadError.get()) {
                throw IllegalStateException("Mapping file not found: $mappingFile")
            }
        }

        // Read the API key and Build ID etc..

        // Construct a basic request
        val manifestInfo = parseManifestInfo()

        // Send the request
        val request = BugsnagMultiPartUploadRequest.from(this)
        val mappingFileHash = mappingFile.md5HashCode()
        val response = uploadRequestClient.get().makeRequestIfNeeded(manifestInfo, mappingFileHash) {
            logger.lifecycle("Bugsnag: Attempting to upload JVM mapping file: $mappingFile")
            request.uploadMultipartEntity(manifestInfo) { builder ->
                builder.addFormDataPart("proguard", mappingFile.name, mappingFile.asRequestBody())

            }
        }
        requestOutputFile.asFile.get().writeText(response)
        logger.lifecycle("Bugsnag: JVM mapping file complete for $mappingFile")
    }

    companion object {

        /**
         * Registers the appropriate subtype to this [project] with the given [name] and
         * [configurationAction]
         */
        internal fun register(
            project: Project,
            name: String,
            configurationAction: BugsnagUploadProguardTask.() -> Unit
        ): TaskProvider<out BugsnagUploadProguardTask> {
            return when {
                project.gradle.versionNumber() >= GradleVersions.VERSION_5_3 -> {
                    project.tasks.register<BugsnagUploadProguardTaskGradle53Plus>(name, configurationAction)
                } else -> {
                    project.tasks.register<BugsnagUploadProguardTaskLegacy>(name, configurationAction)
                }
            }
        }
    }

}

/**
 * Legacy [BugsnagUploadProguardTask] task that requires using [getProject] and
 * [ProjectLayout.configurableFiles].
 */
internal open class BugsnagUploadProguardTaskLegacy @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : BugsnagUploadProguardTask(objects) {

    @get:InputFiles
    override val mappingFileProperty: ConfigurableFileCollection = projectLayout.configurableFiles()
}

internal open class BugsnagUploadProguardTaskGradle53Plus @Inject constructor(
    objects: ObjectFactory
) : BugsnagUploadProguardTask(objects) {

    @get:InputFiles
    override val mappingFileProperty: ConfigurableFileCollection = objects.fileCollection()
}
