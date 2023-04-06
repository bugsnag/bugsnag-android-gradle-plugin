package com.bugsnag.android.gradle.internal

import org.gradle.util.VersionNumber
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

internal object NdkPackageXmlParser {
    private const val PACKAGE_XML_FILENAME = "package.xml"
    private const val TAG_REVISION = "revision"
    private const val TAG_MAJOR = "major"
    private const val TAG_MINOR = "minor"
    private const val TAG_MICRO = "micro"

    internal fun loadVersionFromPackageXml(ndkDir: File): VersionNumber {
        val packageFile = File(ndkDir, PACKAGE_XML_FILENAME)
        var versionNumber: VersionNumber? = null
        if (packageFile.canRead()) {
            versionNumber = loadPackageXml(packageFile) { doc ->
                val revision = doc.getElementsByTagName(TAG_REVISION).item(0)
                val major = revision.getChildValue(TAG_MAJOR)
                val minor = revision.getChildValue(TAG_MINOR)
                val micro = revision.getChildValue(TAG_MICRO)

                VersionNumber(
                    major?.toIntOrNull() ?: 0,
                    minor?.toIntOrNull() ?: 0,
                    micro?.toIntOrNull() ?: 0,
                    null
                )
            }
        }

        return versionNumber ?: VersionNumber.parse(ndkDir.name) // fallback to trying to parse the dir name
    }

    private inline fun <T> loadPackageXml(packageFile: File, action: (Document) -> T): T? {
        return packageFile.inputStream().buffered().use { stream ->
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            try {
                val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val document = builder.parse(stream)
                action(document)
            } catch (ex: Exception) {
                null
            }
        }
    }

    private fun Node.getChildValue(elementName: String): String? {
        val children = childNodes
        repeat(children.length) { index ->
            if (children.item(index).nodeName == elementName) {
                return children.item(index).textContent
            }
        }

        return null
    }
}
