package com.bugsnag.android.gradle.internal

import com.bugsnag.android.gradle.AndroidManifestInfo
import com.bugsnag.android.gradle.GradleVersions
import com.bugsnag.android.gradle.versionNumber
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.FutureTask

sealed class UploadRequestClient : AutoCloseable {

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
    fun makeRequestIfNeeded(
        manifestInfo: AndroidManifestInfo,
        payload: String,
        request: () -> String
    ): String {
        val versionInfoHash = manifestInfo.hashCode()
        val requestIdHash = versionInfoHash + payload.hashCode()

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

/** A [BuildService]-based implementation of [UploadRequestClient]. */
abstract class BuildServiceUploadRequestClient : UploadRequestClient(), BuildService<BuildServiceParameters.None>

/** A simple [UploadRequestClient] for use on Gradle <6.1 */
class LegacyUploadRequestClient : UploadRequestClient()

internal fun newUploadRequestClientProvider(project: Project, prefix: String): Provider<out UploadRequestClient> {
    return if (project.gradle.versionNumber() >= GradleVersions.VERSION_6_1) {
        project.gradle.sharedServices.registerIfAbsent("bugsnag${prefix.capitalize()}UploadRequestClient",
            BuildServiceUploadRequestClient::class.java
        ) {
            // No parameters!
        }
    } else {
        project.provider(::LegacyUploadRequestClient)
    }
}
