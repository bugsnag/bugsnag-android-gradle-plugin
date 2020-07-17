package com.bugsnag.android.gradle

import com.android.Version
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.util.VersionNumber

/**
 * An AGP-4-compatible implementation of [BugsnagManifestUuidTask].
 */
abstract class BugsnagManifestUuidTaskV2 : BaseBugsnagManifestUuidTask() {

    internal companion object {
        private val MIN_AGP_VERSION: VersionNumber = VersionNumber.parse("4.1.0-alpha04")

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
    abstract val inputManifest: RegularFileProperty

    @get:OutputFile
    abstract val outputManifest: RegularFileProperty

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Adds a unique build UUID to AndroidManifest to link proguard mappings to crash reports"
    }

    @TaskAction
    fun updateManifest() {
        val manifestParser = AndroidManifestParser()
        val output = outputManifest.asFile.get()
        manifestParser.writeBuildUuid(
            inputManifest.asFile.get(),
            outputManifest.asFile.get(),
            buildUuid = buildUuid.get()
        )
        writeManifestInfo(manifestParser.readManifest(output, logger))
    }
}
