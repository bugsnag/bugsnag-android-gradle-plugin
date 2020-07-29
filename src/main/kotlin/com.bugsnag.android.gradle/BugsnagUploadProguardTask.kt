package com.bugsnag.android.gradle

import okhttp3.RequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
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

    @get:PathSensitive(NONE)
    @get:InputFile
    val mappingFileProperty: RegularFileProperty = objects.fileProperty()

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val requestOutputFile: RegularFileProperty = objects.fileProperty()

    @get:Input
    override val failOnUploadError: Property<Boolean> = objects.property(Boolean::class.javaObjectType)

    @get:Input
    override val overwrite: Property<Boolean> = objects.property(Boolean::class.javaObjectType)

    @get:Input
    override val endpoint: Property<String> = objects.property(String::class.javaObjectType)

    @get:Input
    override val retryCount: Property<Int> = objects.property(Int::class.javaObjectType)

    @get:Input
    override val timeoutMillis: Property<Long> = objects.property(Long::class.javaObjectType)

    @TaskAction
    fun upload() {
        val mappingFile = mappingFileProperty.asFile.get()
        if (mappingFile.length() == 0L) { // proguard's -dontobfuscate generates an empty mapping file
            project.logger.warn("Bugsnag: Ignoring empty proguard file")
            return
        }
        if (!mappingFile.exists()) {
            logger.warn("Bugsnag: Mapping file not found: $mappingFile")
            val bugsnag = project.extensions.findByType(BugsnagPluginExtension::class.java)!!
            if (bugsnag.failOnUploadError.get()) {
                throw IllegalStateException("Mapping file not found: $mappingFile")
            }
        }

        // Read the API key and Build ID etc..
        logger.lifecycle("Bugsnag: Attempting to upload mapping file: $mappingFile")

        // Construct a basic request
        val parts = mutableMapOf<String, RequestBody>()
        parts["proguard"] = mappingFile.toOctetRequestBody()

        // Send the request
        val request = BugsnagMultiPartUploadRequest.from(this)
        val response = request.uploadMultipartEntity(parts, parseManifestInfo())
        requestOutputFile.asFile.get().writeText(response)
    }

}
