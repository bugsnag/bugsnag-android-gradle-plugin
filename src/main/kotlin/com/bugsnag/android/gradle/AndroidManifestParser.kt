package com.bugsnag.android.gradle

import com.android.utils.forEach
import org.gradle.api.logging.Logger
import org.w3c.dom.Document
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

internal class AndroidManifestParser {

    companion object {
        private const val TAG_MANIFEST = "manifest"
        private const val TAG_APPLICATION = "application"
        private const val TAG_META_DATA = "meta-data"
        private const val TAG_API_KEY = "com.bugsnag.android.API_KEY"
        private const val TAG_BUILD_UUID = "com.bugsnag.android.BUILD_UUID"
        private const val TAG_VERSION_CODE = "com.bugsnag.android.VERSION_CODE"
        private const val TAG_APP_VERSION = "com.bugsnag.android.APP_VERSION"
        private const val ATTR_NAME = "android:name"
        private const val ATTR_VALUE = "android:value"
        private const val ATTR_APPLICATION_ID = "package"
        private const val ATTR_VERSION_CODE = "android:versionCode"
        private const val ATTR_VERSION_NAME = "android:versionName"
    }

    /**
     * Reads information from the AndroidManifest.xml that is required by Bugsnag. This includes
     * the apiKey, versionCode, versionName, applicationId, and buildUUID.
     */
    fun readManifest(manifestPath: File, logger: Logger): AndroidManifestInfo {
        logger.debug("Bugsnag: Reading manifest at: $manifestPath")

        var apiKey: String? = null
        var versionCode: String? = null
        var buildUuid: String? = null
        var versionName: String? = null
        var applicationId: String? = null

        openAndroidManifestXml(manifestPath) { doc ->
            apiKey = findBugsnagMetadataValue(doc, TAG_API_KEY)
            buildUuid = findBugsnagMetadataValue(doc, TAG_BUILD_UUID)
            versionCode = findBugsnagMetadataValue(doc, TAG_VERSION_CODE)
            versionName = findBugsnagMetadataValue(doc, TAG_APP_VERSION)

            val manifest = doc.getElementsByTagName(TAG_MANIFEST).item(0)

            if (manifest != null) {
                applicationId = findManifestAttributeValue(manifest, ATTR_APPLICATION_ID)

                // load defaults if com.bugsnag.android meta-data elements not present
                if (versionCode == null) {
                    versionCode = findManifestAttributeValue(manifest, ATTR_VERSION_CODE)
                }
                if (versionName == null) {
                    versionName = findManifestAttributeValue(manifest, ATTR_VERSION_NAME)
                }
            }
        }

        if (apiKey == null || "" == apiKey || versionCode == null ||
            buildUuid == null || versionName == null || applicationId == null
        ) {
            throw IllegalStateException(
                """Bugsnag: Your AndroidManifest.xml is missing one or more of
                    |apiKey/versionCode/buildUuid/versionName/package, which are required to upload to bugsnag.
                    |apiKey=$apiKey
                    |versionCode=$versionCode
                    |buildUUID=$buildUuid
                    |versionName=$versionName
                    |applicationId=$applicationId
                    |Manifest file = $manifestPath
                """.trimMargin()
            )
        }
        return AndroidManifestInfo(
            requireNotNull(apiKey),
            requireNotNull(versionCode),
            requireNotNull(buildUuid),
            requireNotNull(versionName),
            requireNotNull(applicationId)
        )
    }

    /**
     * Opens the AndroidManifest.xml file and invokes a callback after parsing
     * the XML into a [Document].
     */
    private fun openAndroidManifestXml(manifestPath: File, action: (doc: Document) -> Unit) {
        manifestPath.inputStream().buffered().use { stream ->
            runCatching {
                val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val document = builder.parse(stream)
                action(document)
            }
        }
    }

    /**
     * Finds the value of an attribute in the <manifest> tag, such as android:versionCode.
     * Returns null if this value is not present.
     */
    private fun findManifestAttributeValue(manifest: org.w3c.dom.Node, attrName: String): String? {
        return manifest.attributes.getNamedItem(attrName)?.nodeValue
    }

    /**
     * Finds the value of an attribute in a <meta-data> tag, such as com.bugsnag.android.BUILD_UUID.
     * Returns null if this value is not present.
     */
    private fun findBugsnagMetadataValue(document: Document, attrName: String): String? {
        document.getElementsByTagName(TAG_META_DATA).forEach { node ->
            val key = node.attributes.getNamedItem(ATTR_NAME)?.nodeValue

            if (attrName == key) {
                return node.attributes.getNamedItem(ATTR_VALUE)?.nodeValue
            }
        }
        return null
    }

    /**
     * Writes a buildUUID to the AndroidManifest.xml so that Bugsnag can identify this build
     * for mapping purposes. If a buildUUID is already present, no change is made.
     */
    fun writeBuildUuid(
        manifestPath: File,
        outputPath: File = manifestPath,
        buildUuid: String
    ) {
        openAndroidManifestXml(manifestPath) { document ->
            if (!hasBuildUuid(document)) {
                val application = document.getElementsByTagName(TAG_APPLICATION).item(0)
                val metadata = document.createElement(TAG_META_DATA)
                metadata.setAttribute(ATTR_NAME, TAG_BUILD_UUID)
                metadata.setAttribute(ATTR_VALUE, buildUuid)
                application.appendChild(metadata)
            }

            val factory = TransformerFactory.newInstance()
            val transformer = factory.newTransformer().apply {
                setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
                setOutputProperty(OutputKeys.INDENT, "yes")
                setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            }
            val source = DOMSource(document)
            val output = StreamResult(outputPath)
            transformer.transform(source, output)
        }
    }

    private fun hasBuildUuid(document: Document): Boolean {
        document.getElementsByTagName(TAG_META_DATA).forEach { node ->
            val key = node.attributes.getNamedItem(ATTR_NAME)?.nodeValue

            if (TAG_BUILD_UUID == key) {
                return true
            }
        }
        return false
    }
}
