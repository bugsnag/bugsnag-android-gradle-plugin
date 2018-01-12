package com.bugsnag.android.gradle

/**
 * Defines configuration options (Gradle plugin extensions) for the BugsnagPlugin
 */
class BugsnagPluginExtension {

    String endpoint = 'https://upload.bugsnag.com'
    String releasesEndpoint = 'https://build.bugsnag.com'
    String apiKey = null
    boolean autoUpload = true
    boolean autoReportBuilds = true
    boolean autoProguardConfig = true
    boolean uploadDebugBuildMappings = false
    boolean overwrite = false
    int retryCount = 0
    boolean ndk = false
    String sharedObjectPath = null


    // release API values
    String builderName = null
    Map<String, String> metadata = null

}

class SourceControl {
    String provider = null
    String repository = null
    String revision = null
}
