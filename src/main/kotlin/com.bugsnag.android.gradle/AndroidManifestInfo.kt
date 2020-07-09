package com.bugsnag.android.gradle

data class AndroidManifestInfo(
    var apiKey: String?,
    var versionCode: String?,
    var buildUUID: String?,
    var versionName: String?
)
