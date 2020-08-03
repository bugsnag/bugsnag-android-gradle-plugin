package com.bugsnag.android.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.ArrayList

class PluginExtensionTest {

    lateinit var proj: Project

    @Before
    fun setUp() {
        proj = ProjectBuilder.builder().build()
        proj.pluginManager.apply("com.bugsnag.android.gradle")
        proj.pluginManager.apply("android")
    }

    @Test
    fun ensureExtensionDefaults() {
        val bugsnag = proj.extensions.getByType(BugsnagPluginExtension::class.java)
        assertEquals("https://upload.bugsnag.com", bugsnag.endpoint.get())
        assertTrue(bugsnag.isAutoUpdateBuildUuid)
        assertTrue(bugsnag.isUploadJvmMappings)
        assertTrue(bugsnag.isReportBuilds)
        assertFalse(bugsnag.isUploadDebugBuildMappings)
        assertFalse(bugsnag.overwrite.get())
        assertEquals(0, bugsnag.retryCount.get())
        assertNull(bugsnag.isUploadNdkMappings)
        assertEquals(ArrayList<File>(), bugsnag.sharedObjectPaths)
        assertTrue(bugsnag.failOnUploadError.get())
    }
}
