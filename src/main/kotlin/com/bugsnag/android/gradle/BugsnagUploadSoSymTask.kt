package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariantOutput
import com.bugsnag.android.gradle.internal.AbstractSoMappingTask
import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
import com.bugsnag.android.gradle.internal.NdkToolchain
import com.bugsnag.android.gradle.internal.UploadRequestClient
import com.bugsnag.android.gradle.internal.md5HashCode
import com.bugsnag.android.gradle.internal.taskNameSuffix
import okhttp3.RequestBody.Companion.asRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.net.HttpURLConnection.HTTP_NOT_FOUND

internal abstract class BugsnagUploadSoSymTask : DefaultTask(), AndroidManifestInfoReceiver, BugsnagFileUploadTask {

    @get:InputDirectory
    abstract val symbolFilesDir: DirectoryProperty

    @get:OutputFile
    abstract val requestOutputFile: RegularFileProperty

    @get:Input
    abstract val projectRoot: Property<String>

    @get:Nested
    abstract val ndkToolchain: Property<NdkToolchain>

    @get:Input
    @get:Optional
    abstract val uploadType: Property<UploadType>

    @get:Internal
    internal abstract val uploadRequestClient: Property<UploadRequestClient>

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Uploads SO Symbol files to Bugsnag"
    }

    @TaskAction
    fun upload() {
        val rootDir = symbolFilesDir.asFile.get()
        logger.info("Bugsnag: Found shared object files for upload: $rootDir")

        if (ndkToolchain.get().preferredMappingTool() == NdkToolchain.MappingTool.OBJDUMP) {
            // uploadType == objdump
            val abiDirs = rootDir.listFiles().filter { it.isDirectory }
            abiDirs.forEach { abiDir ->
                val arch = abiDir.name
                abiDir.listFiles()
                    .filter { it.extension == "gz" }
                    .forEach { sharedObjectFile ->
                        uploadObjdump(sharedObjectFile, arch)
                    }
            }
        } else {
            rootDir.walkTopDown()
                .filter { it.isFile && it.extension == "gz" && it.length() >= VALID_SO_FILE_THRESHOLD }
                .forEach { uploadSymbols(it) }
        }
    }

    /**
     * Uploads the given shared object mapping information
     * @param mappingFile the file to upload
     */
    private fun uploadSymbols(mappingFile: File) {
        val sharedObjectName = mappingFile.nameWithoutExtension
        val requestEndpoint = endpoint.get() + ENDPOINT_NDK + ENDPOINT_SUFFIX

        val request = BugsnagMultiPartUploadRequest.from(this, requestEndpoint)
        val manifestInfo = parseManifestInfo()
        val mappingFileHash = mappingFile.md5HashCode()

        val response = uploadRequestClient.get().makeRequestIfNeeded(manifestInfo, mappingFileHash) {
            logger.lifecycle("Bugsnag: Uploading SO mapping file from $mappingFile")
            val body = request.createMultipartBody { builder ->
                builder
                    .addFormDataPart("apiKey", manifestInfo.apiKey)
                    .addFormDataPart("appId", manifestInfo.applicationId)
                    .addFormDataPart("versionCode", manifestInfo.versionCode)
                    .addFormDataPart("versionName", manifestInfo.versionName)
                    .addFormDataPart("soFile", mappingFile.name, mappingFile.asRequestBody())
                    .addFormDataPart("sharedObjectName", sharedObjectName)
                    .addFormDataPart("projectRoot", projectRoot.get())
            }

            request.uploadRequest(body) { response ->
                if (response.code == HTTP_NOT_FOUND && endpoint.get() != UPLOAD_ENDPOINT_DEFAULT) {
                    throw StopExecutionException(
                        "Bugsnag instance does not support the new NDK symbols upload mechanism. " +
                            "Please set legacyNDKSymbolsUpload or upgrade your Bugsnag instance. " +
                            "See https://docs.bugsnag.com/api/ndk-symbol-mapping-upload/ for details."
                    )
                }

                if (!response.isSuccessful) {
                    "Failure"
                } else {
                    response.body!!.string()
                }
            }
        }
        requestOutputFile.asFile.get().writeText(response)
    }

    private fun uploadObjdump(mappingFile: File, arch: String) {
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
                "Bugsnag: Uploading SO mapping file for $sharedObjectName ($arch) from $mappingFile"
            )
            request.uploadMultipartEntity(retryCount.get()) { builder ->
                builder.addAndroidManifestInfo(manifestInfo)
                builder.addFormDataPart(soUploadKey, mappingFile.name, mappingFile.asRequestBody())
                builder.addFormDataPart("arch", arch)
                builder.addFormDataPart("sharedObjectName", sharedObjectName)
                builder.addFormDataPart("projectRoot", projectRoot.get())
            }
        }
        requestOutputFile.asFile.get().writeText(response)
    }

    companion object {
        private const val ENDPOINT_SUFFIX = "/ndk-symbol"

        private const val ENDPOINT_NDK = "/ndk"

        private const val VALID_SO_FILE_THRESHOLD = 1024

        fun taskNameFor(variant: BaseVariantOutput, uploadType: UploadType) =
            "uploadBugsnag${uploadType.name.toLowerCase().replaceFirstChar { it.uppercaseChar() }}" +
                "${variant.baseName.replaceFirstChar { it.uppercaseChar() }}Mapping"

        internal fun requestOutputFileFor(project: Project, output: BaseVariantOutput): Provider<RegularFile> {
            val path = "intermediates/bugsnag/requests/symFor${output.taskNameSuffix()}.json"
            return project.layout.buildDirectory.file(path)
        }

        fun register(
            project: Project,
            variant: BaseVariantOutput,
            ndkToolchain: NdkToolchain,
            uploadType: UploadType,
            generateTaskProvider: TaskProvider<out AbstractSoMappingTask>,
            httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>,
            ndkUploadClientProvider: Provider<out UploadRequestClient>,
        ): TaskProvider<BugsnagUploadSoSymTask> {
            val bugsnag = project.extensions.getByType(BugsnagPluginExtension::class.java)
            return project.tasks.register(
                taskNameFor(variant, uploadType),
                BugsnagUploadSoSymTask::class.java
            ) { task ->
                task.dependsOn(generateTaskProvider)
                task.usesService(ndkUploadClientProvider)

                task.endpoint.set(bugsnag.endpoint)

                task.uploadType.set(uploadType)
                task.ndkToolchain.set(ndkToolchain)

                task.manifestInfo.set(BugsnagManifestUuidTask.manifestInfoForOutput(project, variant))
                task.symbolFilesDir.set(generateTaskProvider.flatMap { it.outputDirectory })
                task.requestOutputFile.set(requestOutputFileFor(project, variant))
                task.projectRoot.set(bugsnag.projectRoot.getOrElse(project.projectDir.toString()))

                task.uploadRequestClient.set(ndkUploadClientProvider)

                task.configureWith(bugsnag, httpClientHelperProvider)
            }
        }
    }

    enum class UploadType(private val path: String, val uploadKey: String) {
        NDK("so-symbol", "soSymbolFile"),
        UNITY("so-symbol-table", "soSymbolTableFile");

        fun endpoint(base: String): String {
            return "$base/$path"
        }
    }
}
