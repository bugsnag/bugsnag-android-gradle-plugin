package com.bugsnag.android.gradle

/**
 * Defines configuration options (Gradle plugin extensions) for the BugsnagPlugin
 */
class BugsnagPluginExtension {
    def String endpoint = 'https://upload.bugsnag.com'
    def String apiKey = null
    def boolean autoUpload = true
    def boolean autoProguardConfig = true
    def boolean uploadDebugBuildMappings = false
    def boolean overwrite = false
    def int retryCount = 0
    def boolean ndk = false
    def String sharedObjectPath = null
}
