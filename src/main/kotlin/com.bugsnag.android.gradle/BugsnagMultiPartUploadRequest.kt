package com.bugsnag.android.gradle

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap
import retrofit2.http.Url
import java.io.File
import java.time.Duration

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
    timeoutDuration: Duration
) {

    private val bugsnagService = createService(timeoutDuration)

    fun uploadMultipartEntity(
        parts: MutableMap<String, RequestBody>,
        manifestInfo: AndroidManifestInfo
    ): String {
        addPropertiesToMultipartEntity(parts, manifestInfo)

        val finalParts = parts.toMap()

        var response = uploadToServer(finalParts)
        var uploadSuccessful = response != null

        // Note - this should eventually be moved to a native OkHttp interceptor
        val maxRetryCount = getRetryCount()
        var retryCount = maxRetryCount
        while (!uploadSuccessful && retryCount > 0) {
            logger.warn(String.format("Bugsnag: Retrying upload (%d/%d) ...",
                maxRetryCount - retryCount + 1, maxRetryCount))
            response = uploadToServer(finalParts)
            uploadSuccessful = response != null
            retryCount--
        }
        if (!uploadSuccessful && failOnUploadError) {
            throw GradleException("Upload did not succeed")
        } else {
            return response!!
        }
    }

    private fun addPropertiesToMultipartEntity(
        parts: MutableMap<String, RequestBody>,
        manifestInfo: AndroidManifestInfo
    ) {
        parts["apiKey"] = manifestInfo.apiKey.toTextRequestBody()
        parts["appId"] = manifestInfo.applicationId.toTextRequestBody()
        parts["versionCode"] = manifestInfo.versionCode.toTextRequestBody()
        parts["buildUUID"] = manifestInfo.buildUUID.toTextRequestBody()
        parts["versionName"] = manifestInfo.versionName.toTextRequestBody()
        if (overwrite) {
            parts["overwrite"] = "true".toTextRequestBody()
        }
        logger.debug("Bugsnag: payload information=$manifestInfo")
    }

    private fun uploadToServer(parts: Map<String, RequestBody>): String? {
        // Make the request
        try {
            val response = bugsnagService.uploadFile(endpoint, parts).execute()
            val statusCode = response.code()
            val responseEntity = response.body()

            if (statusCode != 200) {
                throw IllegalStateException("Bugsnag upload failed with code $statusCode $responseEntity")
            } else {
                return responseEntity
            }
        } catch (exc: Throwable) {
            throw IllegalStateException("Bugsnag Upload failed", exc)
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

        internal fun <T> from(
            task: T
        ): BugsnagMultiPartUploadRequest where T : DefaultTask, T: BugsnagFileUploadTask {
            return BugsnagMultiPartUploadRequest(
                task.logger,
                failOnUploadError = task.failOnUploadError.get(),
                overwrite = task.overwrite.get(),
                endpoint = task.endpoint.get(),
                retryCount = task.retryCount.get(),
                timeoutDuration = Duration.ofMillis(task.timeoutMillis.get())
            )
        }

        private fun createService(timeoutDuration: Duration): BugsnagService {
            return createService(OkHttpClient.Builder()
                .connectTimeout(timeoutDuration)
                .callTimeout(timeoutDuration)
                .build())
        }

        internal fun createService(client: OkHttpClient): BugsnagService {
            return Retrofit.Builder()
                .baseUrl("https://upload.bugsnag.com") // Not actually used
                .validateEagerly(true)
                .callFactory(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create()
        }
    }
}
private val TEXT_PLAIN = "text/plain".toMediaType()
private val OCTET = "application/octet-stream".toMediaType()

internal fun String.toTextRequestBody(): RequestBody {
    return toRequestBody(TEXT_PLAIN)
}

internal fun File.toOctetRequestBody(): RequestBody {
    return asRequestBody(OCTET)
}

internal interface BugsnagService {
    @Multipart
    @POST
    fun uploadFile(
        @Url endpoint: String,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>
    ): retrofit2.Call<String>
}
