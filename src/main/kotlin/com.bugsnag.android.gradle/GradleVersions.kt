package com.bugsnag.android.gradle

import org.gradle.api.invocation.Gradle
import org.gradle.util.VersionNumber

internal object GradleVersions {
    val VERSION_6 = VersionNumber.parse("6.0")
    val VERSION_6_6 = VersionNumber.parse("6.6")
}

internal fun Gradle.versionNumber(): VersionNumber = VersionNumber.parse(gradleVersion)
