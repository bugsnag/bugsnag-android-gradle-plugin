package com.bugsnag.android.gradle.internal

import com.bugsnag.android.gradle.internal.BuildServiceBugsnagHttpClientHelper.Params
import okhttp3.OkHttpClient
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.time.Duration

/** A simple API for providing a shared [OkHttpClient] instance for shared use in upload tasks. */
interface BugsnagHttpClientHelper : AutoCloseable {
    val okHttpClient: OkHttpClient

    override fun close() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
        okHttpClient.cache?.close()
    }
}

/** A [BuildService] implementation of [BugsnagHttpClientHelper]. */
abstract class BuildServiceBugsnagHttpClientHelper
    : BuildService<Params>, BugsnagHttpClientHelper {

    interface Params : BuildServiceParameters {
        val timeoutMillis: Property<Long>
        val retryCount: Property<Int>
    }

    override val okHttpClient: OkHttpClient by lazy {
        newClient(parameters.timeoutMillis.get())
    }
}

/** A simple instance-based [BugsnagHttpClientHelper] for use on Gradle <6.1. */
class LegacyBugsnagHttpClientHelper(
    timeoutMillis: Provider<Long>
) : BugsnagHttpClientHelper {
    override val okHttpClient: OkHttpClient by lazy { newClient(timeoutMillis.get()) }
}

internal fun newClient(timeoutMillis: Long): OkHttpClient {
    val timeoutDuration = Duration.ofMillis(timeoutMillis)
    return OkHttpClient.Builder()
        .readTimeout(Duration.ZERO)
        .writeTimeout(Duration.ZERO)
        .connectTimeout(Duration.ZERO)
        .callTimeout(timeoutDuration)
        .build()
}

internal fun runRequestWithRetries(maxRetries: Int, request: () -> String): String {
    var attempts = 0
    var cause: Throwable?
    do {
        try {
            return request()
        } catch (exc: Throwable) {
            cause = exc
        }
        attempts++
    } while (attempts < maxRetries)
    throw IllegalStateException("Bugsnag request failed to complete", cause)
}
