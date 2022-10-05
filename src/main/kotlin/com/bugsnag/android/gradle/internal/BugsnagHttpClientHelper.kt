package com.bugsnag.android.gradle.internal

import com.bugsnag.android.gradle.BugsnagPluginExtension
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.time.Duration

/**
 * A simple API for providing a shared [OkHttpClient] instance for shared use in upload tasks. This
 * also handles common shared configuration such as timeouts, proxies, etc.
 */
abstract class BugsnagHttpClientHelper : AutoCloseable, BuildService<BugsnagHttpClientHelper.Params> {
    val okHttpClient: OkHttpClient by lazy {
        newClient(parameters.timeoutMillis.get())
    }

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
            return project.gradle.sharedServices.registerIfAbsent(
                "bugsnagHttpClientHelper",
                BugsnagHttpClientHelper::class.java
            ) { spec ->
                // Provide some parameters
                spec.parameters.timeoutMillis.set(bugsnag.requestTimeoutMs)
            }
        }
    }

    interface Params : BuildServiceParameters {
        val timeoutMillis: Property<Long>
    }
}

internal class ProxyAuthenticator(val user: String, val pass: String) : java.net.Authenticator() {
    override fun getPasswordAuthentication() = PasswordAuthentication(user, pass.toCharArray())
}

internal fun newClient(timeoutMillis: Long): OkHttpClient {
    val timeoutDuration = Duration.ofMillis(timeoutMillis)
    val builder = OkHttpClient.Builder()
        .readTimeout(timeoutDuration)
        .writeTimeout(timeoutDuration)
        .connectTimeout(timeoutDuration)
        .callTimeout(Duration.ZERO)
    configureHttpProxySettings(builder)
    return builder.build()
}

private fun configureHttpProxySettings(builder: OkHttpClient.Builder) {
    val host: String? = System.getProperty("http.proxyHost")
    val port: String? = System.getProperty("http.proxyPort")

    if (host != null && port != null) { // set the proxy host/port if set
        val socketAddress = InetSocketAddress(host, port.toInt())
        val proxy = Proxy(Proxy.Type.HTTP, socketAddress)
        builder.proxy(proxy)
    }

    val user: String? = System.getProperty("http.proxyUser")
    val pass: String? = System.getProperty("http.proxyPassword")

    if (user != null && pass != null) { // set the default authenticator if credentials set
        java.net.Authenticator.setDefault(ProxyAuthenticator(user, pass))
        builder.proxyAuthenticator(Authenticator.JAVA_NET_AUTHENTICATOR)
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
