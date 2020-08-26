package com.bugsnag.android.gradle

import com.android.Version
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.util.VersionNumber
import javax.inject.Inject

/**
 * An AGP-4-compatible implementation of [BugsnagManifestUuidTask].
 */
open class BugsnagManifestUuidTaskV2 @Inject constructor(
    objects: ObjectFactory
) : BaseBugsnagManifestUuidTask(objects) {

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
    val inputManifest: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val outputManifest: RegularFileProperty = objects.fileProperty()

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
