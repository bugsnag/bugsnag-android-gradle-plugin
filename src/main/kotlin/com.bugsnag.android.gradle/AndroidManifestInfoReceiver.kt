package com.bugsnag.android.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE

interface AndroidManifestInfoReceiver {

    @get:PathSensitive(NONE)
    @get:InputFile
    val manifestInfoFile: RegularFileProperty

}

internal fun AndroidManifestInfoReceiver.parseManifestInfo(): AndroidManifestInfo {
    return AndroidManifestInfo.read(manifestInfoFile.asFile.get())
}
