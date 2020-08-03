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
class AndroidManifestParseNoUuidTest {

    private val info = AndroidManifestInfo(
        "api-key",
        "12",
        null,
        "5.2",
        "com.example"
    )

    @Mock
    lateinit var logger: Logger

    private lateinit var manifestFile: File

    @Before
    fun setUp() {
        manifestFile = File.createTempFile("manifest_no_uuid", ".xml")
        val classLoader = AndroidManifestParseNoUuidTest::class.java.classLoader
        val res = classLoader.getResource("manifest_no_uuid.xml")!!
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
