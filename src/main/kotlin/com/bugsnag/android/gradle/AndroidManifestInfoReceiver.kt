package com.bugsnag.android.gradle

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface AndroidManifestInfoReceiver {
    @get:Input
    val manifestInfo: Property<AndroidManifestInfo>
}
