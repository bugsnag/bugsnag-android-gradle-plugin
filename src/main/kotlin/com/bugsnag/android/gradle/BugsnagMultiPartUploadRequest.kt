package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.runRequestWithRetries
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import java.io.IOException

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
class BugsnagMultiPartUploadRequest(
    private val failOnUploadError: Boolean,
    private val overwrite: Boolean,
    private val endpoint: String,
    private val okHttpClient: OkHttpClient
) {

    fun uploadMultipartEntity(
        manifestInfo: AndroidManifestInfo,
        retryCount: Int,
        action: (MultipartBody.Builder) -> Unit
    ): String {
        val builder = buildMultipartBody(manifestInfo, overwrite)
        action(builder)
        val body = builder.build()

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

    private fun uploadToServer(body: MultipartBody): String? {
        // Make the request
        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Bugsnag upload failed with code ${response.code}")
            }
            return response.body?.string()
        }
    }

    companion object {

        internal fun buildMultipartBody(manifestInfo: AndroidManifestInfo, overwrite: Boolean): MultipartBody.Builder {
            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("apiKey", manifestInfo.apiKey)
                .addFormDataPart("appId", manifestInfo.applicationId)
                .addFormDataPart("versionCode", manifestInfo.versionCode)
                .addFormDataPart("buildUUID", manifestInfo.buildUUID)
                .addFormDataPart("versionName", manifestInfo.versionName)

            if (overwrite) {
                builder.addFormDataPart("overwrite", "true")
            }
            return builder
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
