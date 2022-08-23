package com.bugsnag.android.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile

interface AndroidManifestInfoReceiver {
    @get:InputFile
    val manifestInfo: RegularFileProperty
}

internal fun AndroidManifestInfoReceiver.parseManifestInfo(): AndroidManifestInfo {
    return AndroidManifestInfo.read(manifestInfo.asFile.get())
}
