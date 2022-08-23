package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.runRequestWithRetries
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.gradle.api.DefaultTask
import java.io.IOException

/**
 * Wrapper for common Bugsnag HTTP multipart upload behaviours.
 */
class BugsnagMultiPartUploadRequest(
    private val failOnUploadError: Boolean,
    private val overwrite: Boolean,
    private val endpoint: String,
    private val okHttpClient: OkHttpClient
) {

    fun uploadMultipartEntity(
        retryCount: Int,
        bodyBuilder: BugsnagMultiPartUploadRequest.(MultipartBody.Builder) -> Unit
    ): String {
        val body = createMultipartBody(bodyBuilder)

        return try {
            runRequestWithRetries(retryCount) {
                uploadToServer(body)!!
            }
        } catch (exc: Throwable) {
            when {
                failOnUploadError -> throw exc
                else -> "Failure"
            }
        }
    }

    fun createMultipartBody(bodyBuilder: BugsnagMultiPartUploadRequest.(MultipartBody.Builder) -> Unit): MultipartBody {
        return buildMultipartBody(overwrite)
            .also { bodyBuilder(it) }
            .build()
    }

    fun MultipartBody.Builder.addAndroidManifestInfo(manifestInfo: AndroidManifestInfo): MultipartBody.Builder {
        return addFormDataPart("apiKey", manifestInfo.apiKey)
            .addFormDataPart("appId", manifestInfo.applicationId)
            .addFormDataPart("versionCode", manifestInfo.versionCode)
            .addFormDataPart("versionName", manifestInfo.versionName)
            .addFormDataPart("buildUUID", manifestInfo.buildUUID)
    }

    fun <R> uploadRequest(body: MultipartBody, responseHandler: (Response) -> R): R {
        // Make the request
        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            return responseHandler(response)
        }
    }

    private fun uploadToServer(body: MultipartBody): String? {
        return uploadRequest(body) { response ->
            if (!response.isSuccessful) {
                throw IOException("Bugsnag upload failed with code ${response.code}")
            }

            response.body?.string()
        }
    }

    companion object {

        internal fun buildMultipartBody(overwrite: Boolean): MultipartBody.Builder {
            return MultipartBody.Builder()
                .apply {
                    if (overwrite) {
                        addFormDataPart("overwrite", "true")
                    }
                }
                .setType(MultipartBody.FORM)
        }

        internal fun <T> from(
            task: T,
            endpoint: String = task.endpoint.get()
        ): BugsnagMultiPartUploadRequest where T : DefaultTask, T : BugsnagFileUploadTask {
            return BugsnagMultiPartUploadRequest(
                failOnUploadError = task.failOnUploadError.get(),
                overwrite = task.overwrite.get(),
                endpoint = endpoint,
                okHttpClient = task.httpClientHelper.get().okHttpClient
            )
        }
    }
}
