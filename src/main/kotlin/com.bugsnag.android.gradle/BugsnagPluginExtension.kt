package com.bugsnag.android.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.util.ConfigureUtil

/**
 * Defines configuration options (Gradle plugin extensions) for the BugsnagPlugin
 */
open class BugsnagPluginExtension {

    val sourceControl: SourceControl = SourceControl()

    var isEnabled = true
    var endpoint = "https://upload.bugsnag.com"
    var releasesEndpoint = "https://build.bugsnag.com"
    var isAutoUpload = true
    var isAutoReportBuilds = true
    var isUploadDebugBuildMappings = false
    var isOverwrite = false
    var retryCount = 0
    var ndk: Boolean? = null
    var sharedObjectPath: String? = null
    var projectRoot: String? = null
    var isFailOnUploadError = true
    var requestTimeoutMs = 60000

    // release API values
    var builderName: String? = null
    var metadata: Map<String, String>? = null
    var objdumpPaths: Map<String, String>? = null

    // exposes sourceControl as a nested object on the extension,
    // see https://docs.gradle.org/current/userguide/custom_gradle_types.html#nested_objects

    fun sourceControl(closure: Closure<SourceControl>) {
        ConfigureUtil.configure(closure, sourceControl)
    }

    fun sourceControl(action: Action<SourceControl>) {
        action.execute(sourceControl)
    }
}
