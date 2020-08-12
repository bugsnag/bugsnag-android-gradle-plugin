package com.bugsnag.android.gradle

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
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
    private val logger: Logger,
    private val failOnUploadError: Boolean,
    private val overwrite: Boolean,
    private val endpoint: String,
    private val retryCount: Int,
    private val okHttpClient: OkHttpClient
) {

    fun uploadMultipartEntity(
        manifestInfo: AndroidManifestInfo,
        action: (MultipartBody.Builder) -> Unit
    ): String {
        val builder = buildMultipartBody(manifestInfo, overwrite)
        action(builder)
        val body = builder.build()


        var response = uploadToServer(body)
        var uploadSuccessful = response != null

        // Note - this should eventually be moved to a native OkHttp interceptor
        val maxRetryCount = getRetryCount()
        var retryCount = maxRetryCount
        while (!uploadSuccessful && retryCount > 0) {
            logger.warn(String.format("Bugsnag: Retrying upload (%d/%d) ...",
                maxRetryCount - retryCount + 1, maxRetryCount))
            response = uploadToServer(body)
            uploadSuccessful = response != null
            retryCount--
        }
        if (!uploadSuccessful && failOnUploadError) {
            throw GradleException("Upload did not succeed")
        } else {
            return response!!
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

    /**
     * Get the retry count defined by the user. If none is set the default is 0 (zero).
     * Also to avoid too much retries the max value is 5 (five).
     *
     * @return the retry count
     */
    private fun getRetryCount(): Int {
        return if (retryCount >= MAX_RETRY_COUNT) MAX_RETRY_COUNT else retryCount
    }

    companion object {
        const val MAX_RETRY_COUNT = 5

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
            task: T
        ): BugsnagMultiPartUploadRequest where T : DefaultTask, T: BugsnagFileUploadTask {
            return BugsnagMultiPartUploadRequest(
                task.logger,
                failOnUploadError = task.failOnUploadError.get(),
                overwrite = task.overwrite.get(),
                endpoint = task.endpoint.get(),
                retryCount = task.retryCount.get(),
                okHttpClient = task.httpClientHelper.get().okHttpClient
            )
        }
    }
}
