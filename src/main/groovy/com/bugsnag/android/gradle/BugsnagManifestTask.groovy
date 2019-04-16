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

    private static final String TAG_META_DATA = 'meta-data'
    private static final String NS_URI_ANDROID = "http://schemas.android.com/apk/res/android"
    private static final String NS_PREFIX_ANDROID = "android"

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

            project.logger.debug("Updating manifest with build UUID: " + manifestPath)

            // Parse the AndroidManifest.xml
            Namespace ns = new Namespace(NS_URI_ANDROID, NS_PREFIX_ANDROID)
            Node xml = new XmlParser().parse(manifestPath)

            def application = xml.application[0]
            if (application) {
                def metaDataTags = application[TAG_META_DATA]

                // remove any old BUILD_UUID_TAG elements
                metaDataTags.findAll {
                    (it.attributes()[ns.name] == BugsnagPlugin.BUILD_UUID_TAG)
                }.each {
                    it.parent().remove(it)
                }

                // Add the new BUILD_UUID_TAG element
                application.appendNode(TAG_META_DATA, [(ns.name): BugsnagPlugin.BUILD_UUID_TAG, (ns.value): buildUUID])

                // Write the manifest file
                FileWriter writer = null

                try {
                    writer = new FileWriter(manifestPath)
                    XmlNodePrinter printer = new XmlNodePrinter(new PrintWriter(writer))
                    printer.preserveWhitespace = true
                    printer.print(xml)
                } catch (IOException e) {
                    project.logger.warn("Failed to update manifest with Bugsnag metadata", e)
                } finally {
                    if (writer != null) {
                        writer.close()
                    }
                }
            } else {
                project.logger.error("Bugsnag detected invalid manifest with no " +
                    "application element so did not write Build UUID")
            }
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
