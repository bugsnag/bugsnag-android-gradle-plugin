package com.bugsnag.android.gradle.internal

import com.bugsnag.android.gradle.AndroidManifestInfo
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.FutureTask

abstract class UploadRequestClient : AutoCloseable, BuildService<BuildServiceParameters.None> {

    private val requestMap = ConcurrentHashMap<String, FutureTask<String>>()

    /**
     * Executes request with automatic de-duplication of similar requests.
     *
     * If the version information and payload match another request that
     * has already been enqueued, the response value of the existing request
     * will be returned. If no existing requests match, a new request will
     * be enqueued and executed.
     *
     * Equality is measured by the [AndroidManifestInfo]
     * and the request payload.
     */
    fun makeRequestIfNeeded(
        manifestInfo: AndroidManifestInfo,
        payloadHash: Int,
        request: () -> String
    ): String {
        val versionInfoHash = manifestInfo.hashCode()
        val requestIdHash = Objects.hash(versionInfoHash, payloadHash)

        val future = requestMap.getOrPut(requestIdHash.toString()) {
            FutureTask { request() }
        }
        future.run()
        return future.get()
    }

    override fun close() {
        requestMap.forEach { (_, future) ->
            future.cancel(true)
        }
    }
}

internal fun newUploadRequestClientProvider(project: Project, prefix: String): Provider<out UploadRequestClient> {
    return project.gradle.sharedServices.registerIfAbsent(
        "bugsnag${prefix.capitalize()}UploadRequestClient",
        UploadRequestClient::class.java
    ) {
        // No parameters!
    }
}
