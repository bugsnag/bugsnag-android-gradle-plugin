package com.bugsnag.android.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input

interface AndroidManifestInfoReceiver {
    @get:Input
    val manifestInfo: RegularFileProperty
}

internal fun AndroidManifestInfoReceiver.parseManifestInfo(): AndroidManifestInfo {
    return AndroidManifestInfo.read(manifestInfo.asFile.get())
}
