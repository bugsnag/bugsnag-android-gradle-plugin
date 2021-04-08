package com.bugsnag.android.gradle

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class AndroidManifestWriteTest {

    private val classLoader = AndroidManifestWriteTest::class.java.classLoader

    private lateinit var manifestFile: File
    private lateinit var outputFile: File

    @Before
    fun setUp() {
        outputFile = File.createTempFile("output", ".xml")
        manifestFile = File.createTempFile("manifest_no_uuid", ".xml")
        val res = classLoader.getResource("manifest_no_uuid.xml")!!
        File(res.file).copyTo(manifestFile, true)
    }

    @After
    fun tearDown() {
        manifestFile.delete()
        outputFile.delete()
    }

    @Test
    fun writeBuildUuid() {
        AndroidManifestParser().writeBuildUuid(manifestFile, outputFile, "build-uuid-123")
        val obs = outputFile.readText().replace("\\s".toRegex(), "")
        val expected = classLoader.getResource("AndroidManifest.xml")!!.readText().replace("\\s".toRegex(), "")
        assertEquals(expected, obs)
    }
}
