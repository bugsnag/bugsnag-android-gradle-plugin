package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
import com.bugsnag.android.gradle.internal.UploadRequestClient
import com.bugsnag.android.gradle.internal.md5HashCode
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import okhttp3.RequestBody.Companion.asRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import javax.inject.Inject

/**
 * Task that uploads shared object mapping files to Bugsnag.
 */
internal open class BugsnagUploadSharedObjectTask @Inject constructor(
    objects: ObjectFactory,
) : DefaultTask(), AndroidManifestInfoReceiver, BugsnagFileUploadTask {

    enum class UploadType(private val path: String, val uploadKey: String) {
        NDK("so-symbol", "soSymbolFile"),
        UNITY("so-symbol-table", "soSymbolTableFile");

        fun endpoint(base: String): String {
            return "$base/$path"
        }
    }

    companion object {
        private const val VALID_SO_FILE_THRESHOLD = 1024

        internal fun register(
            project: Project,
            name: String,
            configurationAction: BugsnagUploadSharedObjectTask.() -> Unit
        ): TaskProvider<BugsnagUploadSharedObjectTask> {
            return project.tasks.register(name, configurationAction)
        }
    }

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Uploads SO mapping files to Bugsnag"
    }

    @get:InputFile
    override val manifestInfo: RegularFileProperty = objects.fileProperty()

    @get:Internal
    internal val uploadRequestClient: Property<UploadRequestClient> = objects.property()

    @get:Internal
    override val httpClientHelper: Property<BugsnagHttpClientHelper> = objects.property()

    @Input
    val projectRoot: Property<String> = objects.property()

    @get:OutputFile
    val requestOutputFile: RegularFileProperty = objects.fileProperty()

    @get:InputDirectory
    val intermediateOutputDir: DirectoryProperty = objects.directoryProperty()

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

    @get:Input
    val uploadType: Property<UploadType> = objects.property()

    @TaskAction
    fun upload() {
        val rootDir = intermediateOutputDir.asFile.get()
        val abiDirs = rootDir.listFiles().filter { it.isDirectory }
        logger.info("Bugsnag: Found shared object files for upload: $abiDirs")

        abiDirs.forEach { abiDir ->
            val arch = abiDir.name
            abiDir.listFiles()
                .filter { it.extension == "gz" }
                .forEach { sharedObjectFile ->
                    uploadSymbols(sharedObjectFile, arch)
                }
        }
    }

    /**
     * Uploads the given shared object mapping information
     * @param mappingFile the file to upload
     * @param arch the arch that is being uploaded
     */
    private fun uploadSymbols(mappingFile: File, arch: String) {
        // a SO file may not contain debug info. if that's the case then the mapping file should be very small,
        // so we try and reject it here as otherwise the event-worker will reject it with a 400 status code.
        if (!mappingFile.exists() || mappingFile.length() < VALID_SO_FILE_THRESHOLD) {
            logger.warn("Bugsnag: Skipping upload of empty/invalid mapping file: $mappingFile")
            return
        }
        val sharedObjectName = mappingFile.nameWithoutExtension
        val requestEndpoint = uploadType.get().endpoint(endpoint.get())
        val soUploadKey = uploadType.get().uploadKey

        val request = BugsnagMultiPartUploadRequest.from(this, requestEndpoint)
        val manifestInfo = parseManifestInfo()
        val mappingFileHash = mappingFile.md5HashCode()
        val response = uploadRequestClient.get().makeRequestIfNeeded(manifestInfo, mappingFileHash) {
            logger.lifecycle(
                "Bugsnag: Uploading SO mapping file for " +
                    "$sharedObjectName ($arch) from $mappingFile"
            )
            request.uploadMultipartEntity(manifestInfo, retryCount.get()) { builder ->
                builder.addFormDataPart(soUploadKey, mappingFile.name, mappingFile.asRequestBody())
                builder.addFormDataPart("arch", arch)
                builder.addFormDataPart("sharedObjectName", sharedObjectName)
                builder.addFormDataPart("projectRoot", projectRoot.get())
            }
        }
        requestOutputFile.asFile.get().writeText(response)
    }
}
