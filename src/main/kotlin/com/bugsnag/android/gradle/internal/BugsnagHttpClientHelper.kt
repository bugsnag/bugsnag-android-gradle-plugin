package com.bugsnag.android.gradle.internal

import com.bugsnag.android.gradle.internal.BuildServiceBugsnagHttpClientHelper.Params
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
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
        newClient(parameters.timeoutMillis.get(), parameters.retryCount.get())
    }
}

/** A simple instance-based [BugsnagHttpClientHelper] for use on Gradle <6.1. */
class LegacyBugsnagHttpClientHelper(
    timeoutMillis: Provider<Long>,
    retryCount: Provider<Int>
) : BugsnagHttpClientHelper {
    override val okHttpClient: OkHttpClient by lazy { newClient(timeoutMillis.get(), retryCount.get()) }
}

internal fun newClient(
    timeoutMillis: Long,
    retryCount: Int
): OkHttpClient {
    val timeoutDuration = Duration.ofMillis(timeoutMillis)
    val interceptor = retryInterceptor(retryCount)
    return OkHttpClient.Builder()
        .readTimeout(Duration.ZERO)
        .writeTimeout(Duration.ZERO)
        .connectTimeout(Duration.ZERO)
        .callTimeout(timeoutDuration)
        .addInterceptor(interceptor)
        .build()
}

internal fun retryInterceptor(maxRetries: Int): Interceptor {
    return object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var response: Response? = null
            var attempts = 1
            var cause: Throwable? = null

            while (response == null && maxRetries >= attempts) {
                try {
                    val request = chain.request()
                    response = chain.proceed(request)
                } catch (exc: Throwable) {
                    cause = exc
                }
                attempts++
            }
            return response ?: throw IllegalStateException("Bugsnag request failed to complete", cause)
        }
    }
}

