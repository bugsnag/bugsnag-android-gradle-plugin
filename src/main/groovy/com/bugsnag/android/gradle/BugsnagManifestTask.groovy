package com.bugsnag.android.gradle

import org.gradle.api.tasks.TaskAction

/**
 Task to add a unique build UUID to AndroidManifest.xml during the build
 process. This is used by Bugsnag to identify which proguard mapping file
 should be used to deobfuscate each crash report.

 https://docs.gradle.org/current/userguide/custom_tasks.html

 This task must be called after "process${variantName}Manifest", since it
 requires that an AndroidManifest.xml exists in `build/intermediates`.
 */
class BugsnagManifestTask extends BugsnagVariantOutputTask {

    BugsnagManifestTask() {
        super()
        this.description = "Adds a unique build UUID to AndroidManifest to link proguard mappings to crash reports"
    }

    @TaskAction
    void updateManifest() {
        List<File> paths = manifestPaths

        // Uniquely identify the build so that we can identify the proguard file.
        String buildUUID = UUID.randomUUID().toString()

        for (File manifestPath in paths) {
            if (!manifestPath.exists()) {
                continue
            }

            ManifestUtil.patchManifest(manifestPath, manifestPath, buildUUID, logger)
        }
    }
}
