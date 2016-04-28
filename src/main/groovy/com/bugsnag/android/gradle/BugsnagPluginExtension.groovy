package com.bugsnag.android.gradle

/**
 * Defines configuration options (Gradle plugin extensions) for the BugsnagPlugin
 */
class BugsnagPluginExtension {
    def boolean enableBugsnag = true;
    def String endpoint = 'https://upload.bugsnag.com'
    def String apiKey = null
    def boolean autoUpload = true;
    def boolean autoProguardConfig = true;
}
