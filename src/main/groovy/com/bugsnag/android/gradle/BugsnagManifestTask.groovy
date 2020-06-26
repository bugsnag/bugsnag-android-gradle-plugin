package com.bugsnag.android.gradle

import groovy.xml.Namespace
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

            ManifestUtil.patchManifest(manifestPath, buildUUID, logger)
        }
    }

    boolean isInstantRun() {
        project.properties["android.optional.compilation"]?.contains("INSTANT_DEV")
    }

    boolean shouldRun() {
        List<File> paths = manifestPaths

        for (File manifestPath in paths) {
            if (!manifestPath.exists()) {
                continue
            }

            Namespace ns = new Namespace(NS_URI_ANDROID, NS_PREFIX_ANDROID)
            def app = new XmlParser().parse(manifestPath).application[0]
            if (app) {
                int tagCount = app[TAG_META_DATA].findAll {
                    (it.attributes()[ns.name] == BugsnagPlugin.BUILD_UUID_TAG)
                }.size()
                if (tagCount == 0 || !isInstantRun()) {
                    return true
                }
            }
        }
        false
    }
}
