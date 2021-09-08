package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariantOutput
import groovy.util.GroovyTestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
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

        val copy = AndroidManifestInfo.read(jsonFile)
        assertEquals(info, copy)
    }

    @Test
    fun testManifestReadForApkVariant() {
        val variantOutput = mock(ApkVariantOutput::class.java)
        `when`(variantOutput.versionCodeOverride).thenReturn(21)
        val variantManifestInfo = info.forApkVariantOutput(variantOutput)

        assertEquals(
            info.copy(versionCode = "21"),
            variantManifestInfo
        )
    }
}
