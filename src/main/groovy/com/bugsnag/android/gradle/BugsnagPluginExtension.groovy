package com.bugsnag.android.gradle

class BugsnagPluginExtension {
    def String endpoint = 'https://upload.bugsnag.com'
    def String apiKey = null
    def boolean autoUpload = true;
    def boolean autoProguardConfig = true;
}
