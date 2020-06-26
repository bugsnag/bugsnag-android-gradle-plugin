package com.bugsnag.android.gradle

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
class BugsnagManifestTaskV2 extends DefaultTask {

    private static MIN_AGP_VERSION = VersionNumber.parse("4.1.0-alpha04")

    // NONE because we only care about its contents, not location.
    @PathSensitive(PathSensitivity.NONE)
    @InputFile
    abstract RegularFileProperty mergedManifest

    @OutputFile
    abstract RegularFileProperty updatedManifest

    BugsnagManifestTaskV2() {
        super()
        this.description = "Adds a unique build UUID to AndroidManifest to link proguard mappings to crash reports"
    }

    @TaskAction
    void updateManifest() {
        // Uniquely identify the build so that we can identify the proguard file.
        String buildUUID = UUID.randomUUID().toString()
        ManifestUtil.patchManifest(mergedManifest.asFile.get(), updatedManifest.asFile.get(), buildUUID, logger)
    }

    static boolean isApplicable() {
        try {
            return VersionNumber.parse(Version.ANDROID_GRADLE_PLUGIN_VERSION) >= MIN_AGP_VERSION
        } catch (Throwable ignored) {
            // Not on a new enough AGP version, return false
            return false
        }
    }
}
