package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
import com.bugsnag.android.gradle.internal.UploadRequestClient
import com.bugsnag.android.gradle.internal.md5HashCode
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import okhttp3.RequestBody.Companion.asRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
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
open class BugsnagUploadProguardTask @Inject constructor(
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
    val mappingFileProperty: RegularFileProperty = objects.fileProperty()

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:Optional
    @get:Input
    override val versionCode: Property<Int> = objects.property()

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
        val mappingFile = mappingFileProperty.get().asFile

        // Read the API key and Build ID etc..

        // Construct a basic request
        val manifestInfo = parseManifestInfo()

        // Send the request
        val request = BugsnagMultiPartUploadRequest.from(this)
        val mappingFileHash = mappingFile.md5HashCode()
        val response = uploadRequestClient.get().makeRequestIfNeeded(manifestInfo, mappingFileHash) {
            request.uploadMultipartEntity(manifestInfo, retryCount.get()) { builder ->
                logger.lifecycle("Bugsnag: Uploading JVM mapping file from: $mappingFile")
                builder.addFormDataPart("proguard", mappingFile.name, mappingFile.asRequestBody())
            }
        }
        requestOutputFile.asFile.get().writeText(response)
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
            return project.tasks.register(name, configurationAction)
        }
    }
}
