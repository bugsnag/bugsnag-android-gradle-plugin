package com.bugsnag.android.gradle

import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import java.io.IOException

internal abstract class Call protected constructor(
    private val retryCountConfig: Provider<Int>,
    private val logger: Logger
) {

    /**
     * Attempts to upload to the server, using automatic retries if unsuccessful
     */
    fun execute() {
        var uploadSuccessful = makeApiCall()
        val maxRetryCount = retryCount
        var retryCount = maxRetryCount
        while (!uploadSuccessful && retryCount > 0) {
            logger.warn(String.format("Bugsnag: Retrying upload (%d/%d) ...",
                maxRetryCount - retryCount + 1, maxRetryCount))
            uploadSuccessful = makeApiCall()
            retryCount--
        }
    }

    /**
     * Makes a request to an API. If the call is not successful, clients should return false for
     * automatic retry logic.
     *
     * @return true if successful, otherwise false.
     */
    @Throws(IOException::class)
    abstract fun makeApiCall(): Boolean

    /**
     * Get the retry count defined by the user. If none is set the default is 0 (zero).
     * Also to avoid too much retries the max value is 5 (five).
     *
     * @return the retry count
     */
    val retryCount: Int
        get() {
            val retryCount = retryCountConfig.get()
            return if (retryCount >= MAX_RETRY_COUNT) MAX_RETRY_COUNT else retryCount
        }

    companion object {
        const val MAX_RETRY_COUNT = 5
    }

}