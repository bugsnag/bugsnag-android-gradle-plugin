package com.bugsnag.android.gradle

/**
 * Defines configuration options (Gradle plugin extensions) for the BugsnagPlugin
 */
open class BugsnagPluginExtension {
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

    var sourceControl: SourceControl? = null

}
