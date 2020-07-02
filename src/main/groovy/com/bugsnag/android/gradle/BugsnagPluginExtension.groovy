package com.bugsnag.android.gradle

import groovy.transform.CompileStatic

/**
 * Defines configuration options (Gradle plugin extensions) for the BugsnagPlugin
 */
@CompileStatic
class BugsnagPluginExtension {

    String endpoint = 'https://upload.bugsnag.com'
    String releasesEndpoint = 'https://build.bugsnag.com'

    boolean autoUpload = true
    boolean autoReportBuilds = true
    boolean uploadDebugBuildMappings = false
    boolean overwrite = false
    int retryCount = 0
    Boolean ndk = null
    String sharedObjectPath = null
    String projectRoot = null
    boolean failOnUploadError = true
    int requestTimeoutMs = 60000

    // release API values
    String builderName = null
    Map<String, String> metadata = null
    Map<String, String> objdumpPaths = null

}

@CompileStatic
class SourceControl {
    String provider = null
    String repository = null
    String revision = null
}
