package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

interface BugsnagFileUploadTask : Task {
    @get:Input
    val failOnUploadError: Property<Boolean>

    @get:Input
    val overwrite: Property<Boolean>

    @get:Input
    val endpoint: Property<String>

    @get:Input
    val retryCount: Property<Int>

    @get:Input
    val timeoutMillis: Property<Long>

    @get:Internal
    val httpClientHelper: Property<BugsnagHttpClientHelper>

    fun configureWith(
        bugsnag: BugsnagPluginExtension,
        httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>
    ) {
        failOnUploadError.set(bugsnag.failOnUploadError)
        overwrite.set(bugsnag.overwrite)
        endpoint.set(bugsnag.endpoint)
        retryCount.set(bugsnag.retryCount)
        timeoutMillis.set(bugsnag.requestTimeoutMs)
        httpClientHelper.set(httpClientHelperProvider)
        usesService(httpClientHelperProvider)
    }
}
