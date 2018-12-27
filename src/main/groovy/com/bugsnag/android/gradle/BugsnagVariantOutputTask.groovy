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
    File getManifestPath() {
        File directory
        def outputDir = variantOutput.processManifest.manifestOutputDirectory

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
        }
        manifestFile
    }

    private String getValueFromStringResources(String keyResId){
        String key = keyResId.substring("@string/".length())
        String mainVariantValue = getStringValueFromVariant("main", key, keyResId)
        String flavorValue = getStringValueFromVariant(variant.flavorName, key, mainVariantValue)
        String buildTypeValue = getStringValueFromVariant(variant.buildType.name, key, flavorValue)
        return getStringValueFromVariant(variant.flavorName + variant.buildType.name.capitalize(), key, buildTypeValue)
    }

    private String getStringValueFromVariant(String flavor, String key, String defValue) {
        String projectPath = getProject().getProjectDir().getPath()
        def valuesFolder = Paths.get(projectPath, "src", flavor, "res", "values").toFile()
        if (!valuesFolder.exists()){
            return defValue
        }
        return Arrays.stream(valuesFolder.listFiles(new StringResourceFilter())).map { f ->
            Node xml = new XmlParser().parse(f)
            return getValueOfString(xml['string'], key)
        }.filter { it != null }.findFirst().orElse(defValue)
    }

    String getValueOfString(NodeList resources, String key) {
        String value = null

        def res = resources.findAll {
            (it.attributes()['name'] == key)
        }
        if (!res.isEmpty()) {
            value = res[0].text()
        }
        return value
    }

    // Read the API key and Build ID etc..
    void readManifestFile() {
        // Parse the AndroidManifest.xml
        Namespace ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
        File manifestPath = getManifestPath()

        if (!manifestPath.exists()) {
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

        if (apiKey != null && apiKey.startsWith("@string/")){
            project.logger.info("apiKey starts with @string '$apiKey' and it will be searched in resources")
            apiKey = getValueFromStringResources(apiKey)
            project.logger.info("apiKey '$apiKey' after search in resources")
        }

        // Get the build version
        versionCode = getVersionCode(xml, ns)
        if (versionCode == null) {
            project.logger.warn("Could not find 'android:versionCode' value in your AndroidManifest.xml")
            return
        }

        // Uniquely identify the build so that we can identify the proguard file.
        buildUUID = getManifestMetaData(metaDataTags, ns, BugsnagPlugin.BUILD_UUID_TAG)

        // Get the version name
        versionName = getVersionName(xml, ns)
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

    private static class StringResourceFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            if (!name.endsWith("xml"))
                return false

            File resource = new File(dir, name)
            Node xml = new XmlParser().parse(resource)
            return xml['string'].size() > 0
        }
    }

}
