package com.bugsnag.android.gradle

import org.gradle.api.logging.Logger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class AndroidManifestParseOverrideTest {

    private val info = AndroidManifestInfo(
        "api-key",
        "103",
        "build-uuid-123",
        "57.2",
        "com.example"
    )

    @Mock
    lateinit var logger: Logger

    private lateinit var manifestFile: File

    @Before
    fun setUp() {
        manifestFile = File.createTempFile("manifest_override", ".xml")
        val classLoader = AndroidManifestParseOverrideTest::class.java.classLoader
        val res = classLoader.getResource("manifest_override.xml")!!
        File(res.file).copyTo(manifestFile, true)
    }

    @After
    fun tearDown() {
        manifestFile.delete()
    }

    @Test
    fun readManifest() {
        val read = AndroidManifestParser().readManifest(manifestFile, logger)
        assertEquals(info, read)
    }
}
