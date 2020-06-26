package com.bugsnag.android.gradle

import groovy.xml.Namespace
import org.gradle.api.logging.Logger

final class ManifestUtil {

    private static final String TAG_META_DATA = 'meta-data'
    private static final String NS_URI_ANDROID = "http://schemas.android.com/apk/res/android"
    private static final String NS_PREFIX_ANDROID = "android"

    private ManifestUtil() {
    }

    static void patchManifest(String inputManifestPath,
        String outputManifestPath,
        String buildUUID,
        Logger logger
    ) {
        logger.debug("Updating manifest with build UUID: " + inputManifestPath)

        // Parse the AndroidManifest.xml
        Namespace ns = new Namespace(NS_URI_ANDROID, NS_PREFIX_ANDROID)
        Node xml = new XmlParser().parse(inputManifestPath)

        def application = xml.application[0]
        if (application) {
            def metaDataTags = application[TAG_META_DATA]

            // if BUILD_UUID is already present don't override what user has specified
            def tags = metaDataTags.findAll {
                (it.attributes()[ns.name] == BugsnagPlugin.BUILD_UUID_TAG)
            }
            if (tags.size() > 0) {
                return
            }

            // Add the new BUILD_UUID_TAG element
            application.appendNode(TAG_META_DATA, [(ns.name):BugsnagPlugin.BUILD_UUID_TAG, (ns.value):buildUUID])

            // Write the manifest file
            FileWriter writer = null

            try {
                writer = new FileWriter(outputManifestPath)
                XmlNodePrinter printer = new XmlNodePrinter(new PrintWriter(writer))
                printer.preserveWhitespace = true
                printer.print(xml)
            } catch (IOException e) {
                logger.warn("Failed to update manifest with Bugsnag metadata", e)
            } finally {
                if (writer != null) {
                    writer.close()
                }
            }
        } else {
            logger.error("Bugsnag detected invalid manifest with no " +
                "application element so did not write Build UUID")
        }
    }
}
