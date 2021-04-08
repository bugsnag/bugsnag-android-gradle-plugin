package com.bugsnag.android.gradle

import org.gradle.api.logging.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class AndroidManifestParseEmptyTest {

    @Mock
    lateinit var logger: Logger

    private lateinit var manifestFile: File

    @Before
    fun setUp() {
        manifestFile = File.createTempFile("manifest_empty", ".xml")
        val classLoader = AndroidManifestParseEmptyTest::class.java.classLoader
        val res = classLoader.getResource("manifest_empty.xml")!!
        File(res.file).copyTo(manifestFile, true)
    }

    @After
    fun tearDown() {
        manifestFile.delete()
    }

    @Test(expected = IllegalStateException::class)
    fun readManifest() {
        AndroidManifestParser().readManifest(manifestFile, logger)
    }
}
