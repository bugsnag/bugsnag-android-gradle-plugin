package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
import org.gradle.api.Task
import org.gradle.api.provider.Property

interface BugsnagFileUploadTask : Task {
    val failOnUploadError: Property<Boolean>
    val overwrite: Property<Boolean>
    val endpoint: Property<String>
    val retryCount: Property<Int>
    val timeoutMillis: Property<Long>
    val httpClientHelper: Property<BugsnagHttpClientHelper>

    fun configureWith(bugsnag: BugsnagPluginExtension) {
        failOnUploadError.set(bugsnag.failOnUploadError)
        overwrite.set(bugsnag.overwrite)
        endpoint.set(bugsnag.endpoint)
        retryCount.set(bugsnag.retryCount)
        timeoutMillis.set(bugsnag.requestTimeoutMs)
    }
}
