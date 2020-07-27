package com.bugsnag.android.gradle

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.FutureTask

class UploadRequestClient {

    private val requestMap = ConcurrentHashMap<String, FutureTask<String>>()

    /**
     * Executes request with automatic de-duplication of similar requests.
     *
     * If the version information and payload match another request that
     * has already been enqueued, the response value of the existing request
     * will be returned. If no existing requests match, a new request will
     * be enqueued and executed.
     *
     * Equality is measured by the [AndroidManifestInfo] (excluding buildUuid)
     * and the request payload.
     */
    fun makeRequestIfNeeded(manifestInfo: AndroidManifestInfo,
                            payload: String,
                            request: () -> String): String {
        val versionInfoHash = manifestInfo.hashCode()
        val requestIdHash = versionInfoHash + payload.hashCode()

        val future = requestMap.getOrPut(requestIdHash.toString()) {
            FutureTask { request() }
        }
        future.run()
        return future.get()
    }
}
