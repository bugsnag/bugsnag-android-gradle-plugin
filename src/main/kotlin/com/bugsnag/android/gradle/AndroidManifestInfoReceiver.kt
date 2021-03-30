package com.bugsnag.android.gradle

import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE

interface AndroidManifestInfoReceiver : Task {
    @get:PathSensitive(NONE)
    @get:InputFile
    val manifestInfoFile: RegularFileProperty

    // versionCode needs to be read separately as the manifestInfoFile is not the final merged
    // manifest, and does not contain the final versionCode on AGP 4.1+.
    @get:Optional
    @get:Input
    val versionCode: Property<Int>
}

internal fun AndroidManifestInfoReceiver.parseManifestInfo(): AndroidManifestInfo {
    return AndroidManifestInfo.read(manifestInfoFile.asFile.get(), versionCode.orNull)
}
