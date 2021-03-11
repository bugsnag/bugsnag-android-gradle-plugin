package com.bugsnag.android.gradle

import groovy.util.GroovyTestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class AndroidManifestInfoTest {

    private val info = AndroidManifestInfo(
        "api-key",
        "12",
        "build-123",
        "5.2",
        "com.example"
    )

    private lateinit var jsonFile: File

    @Before
    fun setUp() {
        jsonFile = File.createTempFile("test", ".json")
    }

    @After
    fun tearDown() {
        jsonFile.delete()
    }

    @Test
    fun testManifestReadWrite() {
        info.write(jsonFile)
        val json = jsonFile.readText()
        assertEquals(
            json,
            """
            {"apiKey":"api-key","versionCode":"12","buildUUID":"build-123","versionName":"5.2","applicationId":"com.example"}
            """.trimIndent()
        )

        val copy = AndroidManifestInfo.read(jsonFile, null)
        assertEquals(info, copy)

        val customCode = AndroidManifestInfo.read(jsonFile, 2)
        assertEquals(info.copy(versionCode = "2"), customCode)
    }
}
