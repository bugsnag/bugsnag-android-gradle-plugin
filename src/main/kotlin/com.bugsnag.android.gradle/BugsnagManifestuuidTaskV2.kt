package com.bugsnag.android.gradle

import org.gradle.api.internal.classpath.ManifestUtil

import com.android.Version
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.util.VersionNumber

/**
An AGP 4.1-compatible version of {@link BugsnagManifestTask}.
 */
abstract class BugsnagManifestuuidTaskV2 : DefaultTask() {

    internal companion object {
        val MIN_AGP_VERSION: VersionNumber = VersionNumber.parse("4.1.0-alpha04")

        fun isApplicable(): Boolean {
            return try {
                VersionNumber.parse(Version.ANDROID_GRADLE_PLUGIN_VERSION) >= MIN_AGP_VERSION
            } catch (ignored: Throwable) {
                // Not on a new enough AGP version, return false
                false
            }
        }
    }

    // NONE because we only care about its contents, not location.
    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val mergedManifest: RegularFileProperty

    @get:OutputFile
    abstract val updatedManifest: RegularFileProperty

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Adds a unique build UUID to AndroidManifest to link proguard mappings to crash reports"
    }

    @TaskAction
    fun updateManifest() {
        val manifestParser = AndroidManifestParser()
        manifestParser.writeBuildUuid(mergedManifest.asFile.get(), updatedManifest.asFile.get())
    }
}
