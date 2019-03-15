package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import groovy.xml.Namespace
import org.gradle.api.DefaultTask

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
        File directory
        File directoryBundle
        List<File> manifestPaths = new ArrayList<>()

        def processManifest = BugsnagPlugin.resolveProcessManifest(variantOutput)
        def outputDir = processManifest.manifestOutputDirectory

        if (outputDir instanceof File) {
            directory = outputDir
        } else {
            // gradle 4.7 introduced a provider API for lazy evaluation of properties,
            // AGP subsequently changed the API from File to Provider<File>
            // see https://docs.gradle.org/4.7/userguide/lazy_configuration.html
            directory = outputDir.get().asFile
        }

        File manifestFile = Paths.get(directory.toString(), variantOutput.dirName, "AndroidManifest.xml").toFile()

        if (!manifestFile.exists()) {
            project.logger.error("Failed to find manifest at ${manifestFile}")
        } else {
            project.logger.info("Found manifest at ${manifestFile}")
            manifestPaths.add(manifestFile)
        }

        // Attempt to get the bundle manifest directory
        if (processManifest.hasProperty("bundleManifestOutputDirectory")) {
            directoryBundle = processManifest.bundleManifestOutputDirectory
            File manifestFileBundle = Paths.get(directoryBundle.toString(), variantOutput.dirName, "AndroidManifest.xml").toFile()
            manifestPaths.add(manifestFileBundle)
        }

        manifestPaths
    }

    // Read the API key and Build ID etc..
    void readManifestFile() {
        def manifestRead = false

        // Parse the AndroidManifest.xml
        Namespace ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
        List<File> manifestPaths = getManifestPaths()

        manifestPaths.each { manifestPath ->

            if (!manifestPath.exists() || manifestRead) {
                return
            }
            project.logger.debug("Reading manifest at: ${manifestPath}")

            Node xml = new XmlParser().parse(manifestPath)
            def metaDataTags = xml.application['meta-data']

            // Get the Bugsnag API key
            apiKey = getApiKey(metaDataTags, ns)
            if (!apiKey) {
                project.logger.warn("Could not find apiKey in '$BugsnagPlugin.API_KEY_TAG' <meta-data> tag in your AndroidManifest.xml or in your gradle config")
            }

            // Get the build version
            versionCode = getVersionCode(xml, ns)
            if (versionCode == null) {
                project.logger.warn("Could not find 'android:versionCode' value in your AndroidManifest.xml")
                return
            }

            // Uniquely identify the build so that we can identify the proguard file.
            buildUUID = getBuildUuid(metaDataTags, ns)

            // Get the version name
            versionName = getVersionName(xml, ns)

            manifestRead = true
        }
    }

    String getApiKey(metaDataTags, Namespace ns) {
        String apiKey

        if (project.bugsnag.apiKey != null) {
            apiKey = project.bugsnag.apiKey
        } else {
            apiKey = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.API_KEY_TAG)
        }
        return apiKey
    }

    String getBuildUuid(metaDataTags, Namespace ns) {
        return getManifestMetaData(metaDataTags, ns, BugsnagPlugin.BUILD_UUID_TAG)
    }

    private String getManifestMetaData(metaDataTags, Namespace ns, String key) {
        String value = null

        def tags = metaDataTags.findAll {
            (it.attributes()[ns.name] == key)
        }
        if (tags.isEmpty()) {
            project.logger.warn("Could not find '$key' <meta-data> tag in your AndroidManifest.xml")
        } else {
            value = tags[0].attributes()[ns.value]
        }
        return value
    }

    String getVersionName(Node xml, Namespace ns) {
        xml.attributes()[ns.versionName]
    }

    String getVersionCode(Node xml, Namespace ns) {
        xml.attributes()[ns.versionCode]
    }

}
