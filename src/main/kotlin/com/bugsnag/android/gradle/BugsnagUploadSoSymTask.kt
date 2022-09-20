package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariantOutput
import com.bugsnag.android.gradle.internal.AbstractSoMappingTask
import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
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
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.net.HttpURLConnection.HTTP_NOT_FOUND

abstract class BugsnagUploadSoSymTask : DefaultTask(), AndroidManifestInfoReceiver, BugsnagFileUploadTask {

    @get:InputDirectory
    abstract val symbolFilesDir: DirectoryProperty

    @get:OutputFile
    abstract val requestOutputFile: RegularFileProperty

    @get:Input
    abstract val projectRoot: Property<String>

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
        rootDir.walkTopDown()
            .filter { it.isFile && it.extension == "gz" && it.length() >= VALID_SO_FILE_THRESHOLD }
            .forEach { uploadSymbols(it) }
    }

    /**
     * Uploads the given shared object mapping information
     * @param mappingFile the file to upload
     */
    private fun uploadSymbols(mappingFile: File) {
        val sharedObjectName = mappingFile.nameWithoutExtension
        val requestEndpoint = endpoint.get() + ENDPOINT_SUFFIX

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

    companion object {
        private const val ENDPOINT_SUFFIX = "/ndk-symbol"

        private const val VALID_SO_FILE_THRESHOLD = 1024

        fun taskNameFor(variant: BaseVariantOutput) =
            "uploadBugsnag${variant.baseName.capitalize()}Symbols"

        internal fun requestOutputFileFor(project: Project, output: BaseVariantOutput): Provider<RegularFile> {
            val path = "intermediates/bugsnag/requests/symFor${output.taskNameSuffix()}.json"
            return project.layout.buildDirectory.file(path)
        }

        fun register(
            project: Project,
            variant: BaseVariantOutput,
            generateTaskProvider: TaskProvider<out AbstractSoMappingTask>,
            httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>,
            ndkUploadClientProvider: Provider<out UploadRequestClient>,
        ): TaskProvider<BugsnagUploadSoSymTask> {
            val bugsnag = project.extensions.getByType(BugsnagPluginExtension::class.java)
            return project.tasks.register(taskNameFor(variant), BugsnagUploadSoSymTask::class.java) { task ->
                task.dependsOn(generateTaskProvider)
                task.usesService(httpClientHelperProvider)
                task.usesService(ndkUploadClientProvider)

                task.endpoint.set(bugsnag.endpoint)

                task.manifestInfo.set(BugsnagManifestUuidTask.manifestInfoForOutput(project, variant))
                task.symbolFilesDir.set(generateTaskProvider.flatMap { it.outputDirectory })
                task.requestOutputFile.set(requestOutputFileFor(project, variant))
                task.projectRoot.set(bugsnag.projectRoot.getOrElse(project.projectDir.toString()))

                task.httpClientHelper.set(httpClientHelperProvider)
                task.uploadRequestClient.set(ndkUploadClientProvider)

                task.configureWith(bugsnag)
            }
        }
    }
}
