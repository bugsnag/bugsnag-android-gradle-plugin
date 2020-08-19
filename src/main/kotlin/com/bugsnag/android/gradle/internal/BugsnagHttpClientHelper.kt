package com.bugsnag.android.gradle.internal

import com.bugsnag.android.gradle.BugsnagPluginExtension
import com.bugsnag.android.gradle.internal.BuildServiceBugsnagHttpClientHelper.Params
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.time.Duration

/**
 * A simple API for providing a shared [OkHttpClient] instance for shared use in upload tasks. This
 * also handles common shared configuration such as timeouts, proxies, etc.
 */
interface BugsnagHttpClientHelper : AutoCloseable {
    val okHttpClient: OkHttpClient

    override fun close() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
        okHttpClient.cache?.close()
    }

    companion object {
        fun create(
            project: Project,
            bugsnag: BugsnagPluginExtension
        ): Provider<out BugsnagHttpClientHelper> {
            val canUseBuildService = project.gradle.versionNumber() >= GradleVersions.VERSION_6_1
            return if (canUseBuildService) {
                project.gradle.sharedServices.registerIfAbsent("bugsnagHttpClientHelper",
                    BuildServiceBugsnagHttpClientHelper::class.java
                ) { spec ->
                    // Provide some parameters
                    spec.parameters.timeoutMillis.set(bugsnag.requestTimeoutMs)
                    spec.parameters.retryCount.set(bugsnag.retryCount)
                }
            } else {
                // Reuse instance
                val client = LegacyBugsnagHttpClientHelper(
                    bugsnag.requestTimeoutMs,
                    bugsnag.retryCount
                )
                project.provider { client }
            }
        }
    }
}

/** A [BuildService] implementation of [BugsnagHttpClientHelper]. */
internal abstract class BuildServiceBugsnagHttpClientHelper
    : BuildService<Params>, BugsnagHttpClientHelper {

    interface Params : BuildServiceParameters {
        val timeoutMillis: Property<Long>
        val retryCount: Property<Int>
    }

    override val okHttpClient: OkHttpClient by lazy {
        newClient(
            parameters.timeoutMillis.get(),
            parameters.retryCount.get()
        )
    }
}

/** A simple instance-based [BugsnagHttpClientHelper] for use on Gradle <6.1. */
internal class LegacyBugsnagHttpClientHelper(
    timeoutMillis: Provider<Long>,
    retryCount: Provider<Int>
) : BugsnagHttpClientHelper {
    override val okHttpClient: OkHttpClient by lazy {
        newClient(
            timeoutMillis.get(),
            retryCount.get()
        )
    }
}

@Suppress("LongParameterList")
private fun newClient(
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
        .proxyAuthenticator(Authenticator.JAVA_NET_AUTHENTICATOR)
        .build()
}

private fun retryInterceptor(maxRetries: Int): Interceptor {
    return object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var attempts = 0
            var cause: Throwable?
            do {
                try {
                    val request = chain.request()
                    return chain.proceed(request)
                } catch (exc: Throwable) {
                    cause = exc
                }
                attempts++
            } while (attempts < maxRetries)
            throw IllegalStateException("Bugsnag request failed to complete", cause)
        }
    }
}

