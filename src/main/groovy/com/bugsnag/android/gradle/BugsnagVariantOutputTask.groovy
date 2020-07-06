package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ManifestProcessorTask
import groovy.xml.Namespace
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory

import java.nio.file.Paths

class BugsnagVariantOutputTask extends DefaultTask {

    BaseVariantOutput variantOutput
    BaseVariant variant

    // Read from the manifest file
    String apiKey
    String versionCode
    String buildUUID
    String versionName

    /**
     * Gets the manifest for a given Variant Output, accounting for any APK splits.
     *
     * Currently supported split types include Density, and ABI. There is also a Language split,
     * but it appears to be broken (see issuetracker)
     *
     * @param output the variant output
     * @return the manifest path
     *
     * See: https://developer.android.com/studio/build/configure-apk-splits.html#build-apks-filename
     * https://issuetracker.google.com/issues/37085185
     */
    List<File> getManifestPaths() {
        File directoryMerged
        File directoryBundle
        List<File> manifestPaths = new ArrayList<>()

        boolean getMergedManifest = BugsnagPlugin.isRunningAssembleTask(variant, variantOutput, project)
        boolean getBundleManifest = BugsnagPlugin.isRunningBundleTask(variant, variantOutput, project)

        // If the manifest location could not be reliably determined, attempt to get both
        if (!getMergedManifest && !getBundleManifest) {
            getMergedManifest = true
            getBundleManifest = true
        }

        ManifestProcessorTask processManifest = variantOutput.processManifestProvider.getOrNull()

        if (processManifest == null) {
            return manifestPaths
        }

        if (getMergedManifest) {
            Object outputDir = processManifest.manifestOutputDirectory
            Directory dir = outputDir.getOrNull()

            if (dir != null) {
                directoryMerged = dir.asFile
            }

            if (directoryMerged != null) {
                addManifestPath(manifestPaths, directoryMerged)
            }
        }

        // Attempt to get the bundle manifest directory if required
        if (getBundleManifest) {
            directoryBundle = BugsnagPlugin.resolveBundleManifestOutputDirectory(processManifest)

            if (directoryBundle != null) {
                addManifestPath(manifestPaths, directoryBundle)
            }
        }

        manifestPaths
    }

    void addManifestPath(List<File> manifestPaths, File directory) {
        File manifestFile = Paths.get(directory.toString(), variantOutput.dirName,
            "AndroidManifest.xml").toFile()

        if (manifestFile.exists()) {
            project.logger.info("Found manifest at ${manifestFile}")
            manifestPaths.add(manifestFile)
        } else {
            project.logger.error("Failed to find manifest at ${manifestFile}")
        }
    }

    // Read the API key and Build ID etc..
    void readManifestFile() {
        // Parse the AndroidManifest.xml
        Namespace ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
        List<File> paths = manifestPaths

        for (File manifestPath in paths) {
            if (!manifestPath.exists()) {
                continue
            }

            project.logger.debug("Reading manifest at: ${manifestPath}")

            Node xml = new XmlParser().parse(manifestPath)
            def metaDataTags = xml.application["meta-data"]

            // If the current manifest does not contain the build ID then try the next manifest in the list (if any)
            if (!(manifestPath == paths.last()) && !hasBuildUuid(metaDataTags, ns)) {
                continue
            }

            // Get the Bugsnag API key
            apiKey = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.API_KEY_TAG)
            if (apiKey == null) {
                project.logger.warn("Could not find apiKey in '$BugsnagPlugin.API_KEY_TAG' " +
                    "<meta-data> tag in your AndroidManifest.xml")
            }

            // Get the build version
            versionCode = getVersionCode(metaDataTags, xml, ns)
            if (versionCode == null) {
                project.logger.warn("Could not find 'android:versionCode' value in your AndroidManifest.xml")
                continue
            }

            // Uniquely identify the build so that we can identify the proguard file.
            buildUUID = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.BUILD_UUID_TAG)
            if (buildUUID == null) {
                project.logger.warn("Could not find '$BugsnagPlugin.BUILD_UUID_TAG'" +
                    " <meta-data> tag in your AndroidManifest.xml")
            }

            // Get the version name
            versionName = getVersionName(metaDataTags, xml, ns)
            if (versionName == null) {
                project.logger.warn("Could not find 'android:versionName' value in your AndroidManifest.xml")
            }
            return
        }
    }

    boolean hasBuildUuid(metaDataTags, Namespace ns) {
        metaDataTags.any {
            it.attributes()[ns.name] == BugsnagPlugin.BUILD_UUID_TAG
        }
    }

    private String getManifestMetaData(metaDataTags, Namespace ns, String key) {
        String value = null

        def tags = metaDataTags.findAll {
            (it.attributes()[ns.name] == key)
        }
        if (!tags.isEmpty()) {
            value = tags[0].attributes()[ns.value]
        }
        value
    }

    String getVersionName(metaDataTags, Node xml, Namespace ns) {
        String versionName = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.APP_VERSION_TAG)

        if (versionName != null) {
            return versionName
        } else {
            return xml.attributes()[ns.versionName]
        }
    }

    String getVersionCode(metaDataTags, Node xml, Namespace ns) {
        String versionCode = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.VERSION_CODE_TAG)

        if (versionCode != null) {
            return versionCode
        } else {
            return xml.attributes()[ns.versionCode]
        }
    }
}
