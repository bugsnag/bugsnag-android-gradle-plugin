package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
import com.bugsnag.android.gradle.internal.UploadRequestClient
import com.bugsnag.android.gradle.internal.md5HashCode
import com.bugsnag.android.gradle.internal.property
import okhttp3.RequestBody.Companion.asRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
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
    val mappingFileProperty: ConfigurableFileCollection = objects.fileCollection()

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

}
