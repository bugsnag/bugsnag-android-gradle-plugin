package com.bugsnag.android.gradle

import org.gradle.api.file.RegularFileProperty

interface AndroidManifestInfoReceiver {
    val manifestInfoFile: RegularFileProperty
}

internal fun AndroidManifestInfoReceiver.parseManifestInfo(): AndroidManifestInfo {
    return AndroidManifestInfo.read(manifestInfoFile.asFile.get())
}
