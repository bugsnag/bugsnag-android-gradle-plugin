package com.bugsnag.android.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

interface AndroidManifestInfoReceiver {
    val manifestInfoFile: RegularFileProperty
    val versionCode: Property<Int>
}

internal fun AndroidManifestInfoReceiver.parseManifestInfo(): AndroidManifestInfo {
    return AndroidManifestInfo.read(manifestInfoFile.asFile.get(), versionCode.orNull)
}
