package com.bugsnag.android.gradle;

import groovy.util.Node;
import groovy.util.NodeList;
import groovy.util.XmlNodePrinter;
import groovy.util.XmlParser;
import groovy.xml.Namespace;
import org.gradle.api.tasks.TaskAction;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 Task to add a unique build UUID to AndroidManifest.xml during the build
 process. This is used by Bugsnag to identify which proguard mapping file
 should be used to deobfuscate each crash report.

 https://docs.gradle.org/current/userguide/custom_tasks.html

 This task must be called after "process${variantName}Manifest", since it
 requires that an AndroidManifest.xml exists in `build/intermediates`.
 */
public class BugsnagManifestTask extends BugsnagVariantOutputTask {

    private static final String TAG_META_DATA = "meta-data";
    private static final String NS_URI_ANDROID = "http://schemas.android.com/apk/res/android";
    private static final String NS_PREFIX_ANDROID = "android";

    @TaskAction
    void updateManifest() throws ParserConfigurationException, SAXException, IOException {
        List<File> paths = getManifestPaths();

        // Uniquely identify the build so that we can identify the proguard file.
        String buildUUID = UUID.randomUUID().toString();

        for (File manifestPath : paths) {
            if (!manifestPath.exists()) {
                continue;
            }

            getProject().getLogger().debug("Updating manifest with build UUID: " + manifestPath);

            // Parse the AndroidManifest.xml
            Namespace ns = new Namespace(NS_URI_ANDROID, NS_PREFIX_ANDROID);
            Node xml = new XmlParser().parse(manifestPath);

            Node application = (Node)((NodeList) xml.get("application")).get(0);
            List<Node> metaDataTags = new ArrayList();

            for (Object child : application.children()) {
                if (child instanceof Node) {
                    Node node = (Node) child;
                    if ("meta-data".equals(node.name())) {
                        metaDataTags.add(node);
                    }
                }
            }

            // If the current manifest does not contain the build ID then try the next manifest in the list (if any)
            if (!hasBuildUuid(metaDataTags, ns)) {
                // Add the new BUILD_UUID_TAG element
                Map attrs = new HashMap();
                attrs.put(ns.get("name"), BugsnagPlugin.BUILD_UUID_TAG);
                attrs.put(ns.get("value"), buildUUID);
                application.appendNode(TAG_META_DATA, attrs);

                // Write the manifest file
                FileWriter writer = null;

                try {
                    writer = new FileWriter(manifestPath);
                    XmlNodePrinter printer = new XmlNodePrinter(new PrintWriter(writer));
                    printer.setPreserveWhitespace(true);
                    printer.print(xml);
                } catch (IOException e) {
                    getProject().getLogger().warn("Failed to update manifest with Bugsnag metadata", e);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }
        }
    }

    boolean hasBuildUuid(List<Node> metaDataTags, Namespace ns) {
        for (Object obj: metaDataTags) {
            Node node = (Node) obj;

            Object name = node.attribute(ns.get("name"));

            if (BugsnagPlugin.BUILD_UUID_TAG.equals(name)) {
                Object buildUuid = node.attribute(ns.get("value"));
                return buildUuid != null;
            }
        }
        return false;
    }
}
