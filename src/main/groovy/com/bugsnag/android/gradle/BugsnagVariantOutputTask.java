package com.bugsnag.android.gradle;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.tasks.ManifestProcessorTask;
import groovy.util.Node;
import groovy.util.NodeList;
import groovy.util.XmlParser;
import groovy.xml.Namespace;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BugsnagVariantOutputTask extends DefaultTask {

    BaseVariantOutput variantOutput;
    BaseVariant variant;

    // Read from the manifest file
    String apiKey;
    String versionCode;
    String buildUUID;
    String versionName;

    /**
     * Gets the manifest for a given Variant Output, accounting for any APK splits.
     *
     * Currently supported split types include Density, and ABI. There is also a Language split,
     * but it appears to be broken (see issuetracker)
     *
     * @return the manifest path
     *
     * See: https://developer.android.com/studio/build/configure-apk-splits.html#build-apks-filename
     * https://issuetracker.google.com/issues/37085185
     */
    List<File> getManifestPaths() {
        File directoryMerged = null;
        File directoryBundle;
        List<File> manifestPaths = new ArrayList();

        boolean getMergedManifest = BugsnagPlugin.isRunningAssembleTask(variant, variantOutput, getProject());
        boolean getBundleManifest = BugsnagPlugin.isRunningBundleTask(variant, variantOutput, getProject());

        // If the manifest location could not be reliably determined, attempt to get both
        if (!getMergedManifest && !getBundleManifest) {
            getMergedManifest = true;
            getBundleManifest = true;
        }

        ManifestProcessorTask processManifest = variantOutput.getProcessManifestProvider().getOrNull();

        if (processManifest == null) {
            return manifestPaths;
        }

        if (getMergedManifest) {
            Provider<Directory> outputDir = processManifest.getManifestOutputDirectory();
            Directory dir = outputDir.getOrNull();

            if (dir != null) {
                directoryMerged = dir.getAsFile();
            }

            if (directoryMerged != null) {
                addManifestPath(manifestPaths, directoryMerged);
            }
        }

        // Attempt to get the bundle manifest directory if required
        if (getBundleManifest) {
            directoryBundle = BugsnagPlugin.resolveBundleManifestOutputDirectory(processManifest);

            if (directoryBundle != null) {
                addManifestPath(manifestPaths, directoryBundle);
            }
        }

        return manifestPaths;
    }

    void addManifestPath(List<File> manifestPaths, File directory) {
        File manifestFile = Paths.get(directory.toString(), variantOutput.getDirName(),
            "AndroidManifest.xml").toFile();

        if (manifestFile.exists()) {
            getProject().getLogger().info("Found manifest at ${manifestFile}");
            manifestPaths.add(manifestFile);
        } else {
            getProject().getLogger().error("Failed to find manifest at ${manifestFile}");
        }
    }

    // Read the API key and Build ID etc..
    void readManifestFile() throws ParserConfigurationException, SAXException, IOException {
        // Parse the AndroidManifest.xml
        Namespace ns = new Namespace("http://schemas.android.com/apk/res/android", "android");
        List<File> paths = getManifestPaths();


        for (File manifestPath : paths) {
            if (!manifestPath.exists()) {
                continue;
            }

            getProject().getLogger().debug("Reading manifest at: ${manifestPath}");
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
                continue;
            }

            // Get the Bugsnag API key
            apiKey = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.API_KEY_TAG);
            if (apiKey == null) {
                getProject().getLogger().warn("Could not find apiKey in '$BugsnagPlugin.API_KEY_TAG' " +
                    "<meta-data> tag in your AndroidManifest.xml");
            }

            // Get the build version
            versionCode = getVersionCode(metaDataTags, xml, ns);
            if (versionCode == null) {
                getProject().getLogger().warn("Could not find 'android:versionCode' value in your AndroidManifest.xml");
                continue;
            }

            // Uniquely identify the build so that we can identify the proguard file.
            buildUUID = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.BUILD_UUID_TAG);
            if (buildUUID == null) {
                getProject().getLogger().warn("Could not find '$BugsnagPlugin.BUILD_UUID_TAG'" +
                    " <meta-data> tag in your AndroidManifest.xml");
            }

            // Get the version name
            versionName = getVersionName(metaDataTags, xml, ns);
            if (versionName == null) {
                getProject().getLogger().warn("Could not find 'android:versionName' value in your AndroidManifest.xml");
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
            getLogger().lifecycle("Found build uuid element" + name);

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
