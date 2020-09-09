package com.bugsnag.android.gradle.internal

import com.bugsnag.android.gradle.internal.BuildServiceBugsnagHttpClientHelper.Params
import me.tongfei.progressbar.DelegatingProgressBarConsumer
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import okhttp3.Response
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import retrofit2.Invocation
import java.io.IOException
import java.time.Duration
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION

private const val ONE_MEGABYTE_IN_BYTES: Long = 1 * 1024 * 1024

/** A simple API for providing a shared [OkHttpClient] instance for shared use in upload tasks. */
interface BugsnagHttpClientHelper : AutoCloseable {
    val okHttpClient: OkHttpClient

    override fun close() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
        okHttpClient.cache?.close()
    }

}

/** Annotate a Retrofit endpoint to indicate that it should display upload progress. */
@Target(FUNCTION)
@Retention(RUNTIME)
internal annotation class DisplayProgress(val name: String)

/** A [BuildService] implementation of [BugsnagHttpClientHelper]. */
abstract class BuildServiceBugsnagHttpClientHelper
    : BuildService<Params>, BugsnagHttpClientHelper {

    interface Params : BuildServiceParameters {
        val timeoutMillis: Property<Long>
        val retryCount: Property<Int>
        val displayProgress: Property<Boolean>
    }

    override val okHttpClient: OkHttpClient by lazy {
        newClient(
            parameters.timeoutMillis.get(),
            parameters.displayProgress.get()
        )
    }
}

/** A simple instance-based [BugsnagHttpClientHelper] for use on Gradle <6.1. */
class LegacyBugsnagHttpClientHelper(
    timeoutMillis: Provider<Long>,
    displayProgress: Provider<Boolean>
) : BugsnagHttpClientHelper {
    override val okHttpClient: OkHttpClient by lazy {
        newClient(timeoutMillis.get(), displayProgress.get())
    }
}

internal fun newClient(
    timeoutMillis: Long,
    displayProgress: Boolean
): OkHttpClient {
    val timeoutDuration = Duration.ofMillis(timeoutMillis)
    return OkHttpClient.Builder()
        .readTimeout(timeoutDuration)
        .writeTimeout(timeoutDuration)
        .connectTimeout(timeoutDuration)
        .callTimeout(Duration.ZERO)
        .apply {
            if (displayProgress) {
                addInterceptor(ProgressInterceptor())
            }
        }
        .build()
}

internal class ProgressInterceptor: Interceptor {
    @Suppress("NestedBlockDepth")
    override fun intercept(chain: Chain): Response {
        var request = chain.request()
        var progressListener: ProgressListener? = null
        if (request.method == "POST") {
            // Get the retrofit invocation off of it
            val invocation = request.tag(Invocation::class.java)
            if (invocation != null) {
                // If we requested to display progress, create a ProgressRequestBody wrapper
                val displayProgress = invocation.method()
                    .getAnnotation(DisplayProgress::class.java)
                if (displayProgress != null) {
                    request = request.body?.let { requestBody ->
                        val listener = progressListener(displayProgress.name)
                        progressListener = listener
                        request.newBuilder()
                            .post(ProgressRequestBody(requestBody, listener))
                            .build()
                    } ?: request

                }
            }
        }

        return try {
            chain.proceed(request)
        } catch (e: IOException) {
            progressListener?.close()
            throw e
        }
    }
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

internal fun progressListener(taskName: String) = object : ProgressListener {
    private lateinit var progressBar: ProgressBar

    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
        if (done) {
            if (::progressBar.isInitialized) {
                progressBar.close()
            }
            System.out.flush()
        } else {
            if (!::progressBar.isInitialized) {
                if (contentLength == -1L) {
                    error("content-length: unknown")
                } else {
                    progressBar = ProgressBarBuilder()
                        .setTaskName(taskName)
                        .setInitialMax(contentLength)
                        .setStyle(ProgressBarStyle.ASCII)
                        .setConsumer(
                            DelegatingProgressBarConsumer {
                                print("\r$it")
                                System.out.flush()
                            }
                        )
                        .setUnit("MB", ONE_MEGABYTE_IN_BYTES)
                        .build()
                }
            }
            if (contentLength != -1L) {
                progressBar.stepTo(bytesRead)
            }
        }
    }

    override fun close() {
        if (::progressBar.isInitialized) {
            progressBar.close()
        }
    }
}

