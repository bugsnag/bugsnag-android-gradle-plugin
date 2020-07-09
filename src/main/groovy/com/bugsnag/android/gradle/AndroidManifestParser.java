package com.bugsnag.android.gradle;

import groovy.util.Node;
import groovy.util.NodeList;
import groovy.util.XmlNodePrinter;
import groovy.util.XmlParser;
import groovy.xml.Namespace;
import org.gradle.api.logging.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidManifestParser {

    private static final String TAG_META_DATA = "meta-data";

    public AndroidManifestInfo readManifest(File manifestPath, Logger logger) throws ParserConfigurationException, SAXException, IOException {
        logger.debug("Reading manifest at: ${manifestPath}");
        Namespace ns = new Namespace("http://schemas.android.com/apk/res/android", "android");
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
        }

        // Get the Bugsnag API key
        String apiKey = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.API_KEY_TAG);
        if (apiKey == null) {
            logger.warn("Could not find apiKey in '$BugsnagPlugin.API_KEY_TAG' " +
                "<meta-data> tag in your AndroidManifest.xml");
        }

        // Get the build version
        String versionCode = getVersionCode(metaDataTags, xml, ns);
        if (versionCode == null) {
            logger.warn("Could not find 'android:versionCode' value in your AndroidManifest.xml");
        }

        // Uniquely identify the build so that we can identify the proguard file.
        String buildUUID = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.BUILD_UUID_TAG);
        if (buildUUID == null) {
            logger.warn("Could not find '$BugsnagPlugin.BUILD_UUID_TAG'" +
                " <meta-data> tag in your AndroidManifest.xml");
        }

        // Get the version name
        String versionName = getVersionName(metaDataTags, xml, ns);
        if (versionName == null) {
            logger.warn("Could not find 'android:versionName' value in your AndroidManifest.xml");
        }
        return new AndroidManifestInfo(apiKey, versionCode, buildUUID, versionName);
    }

    public void writeBuildUuid(File manifestPath, String buildUuid, Logger logger) throws ParserConfigurationException, SAXException, IOException {
        // Parse the AndroidManifest.xml
        Namespace ns = new Namespace("http://schemas.android.com/apk/res/android", "android");
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
            attrs.put(ns.get("value"), buildUuid);
            application.appendNode(TAG_META_DATA, attrs);

            // Write the manifest file
            FileWriter writer = null;

            try {
                writer = new FileWriter(manifestPath);
                XmlNodePrinter printer = new XmlNodePrinter(new PrintWriter(writer));
                printer.setPreserveWhitespace(true);
                printer.print(xml);
            } catch (IOException e) {
                logger.warn("Failed to update manifest with Bugsnag metadata", e);
            } finally {
                if (writer != null) {
                    writer.close();
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

    private String getManifestMetaData(List<Node> metaDataTags, Namespace ns, String key) {
        for (Object obj: metaDataTags) {
            Node node = (Node) obj;
            Object name = node.attribute(ns.get("name"));

            if (key.equals(name)) {
                return (String) node.attribute(ns.get("value"));
            }
        }
        return null;
    }

    String getVersionName(List<Node> metaDataTags, Node xml, Namespace ns) {
        String versionName = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.APP_VERSION_TAG);

        if (versionName != null) {
            return versionName;
        } else {
            return (String) xml.attribute(ns.get("versionName"));
        }
    }

    String getVersionCode(List<Node> metaDataTags, Node xml, Namespace ns) {
        String versionCode = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.VERSION_CODE_TAG);

        if (versionCode != null) {
            return versionCode;
        } else {
            return (String) xml.attribute(ns.get("versionCode"));
        }
    }
}
