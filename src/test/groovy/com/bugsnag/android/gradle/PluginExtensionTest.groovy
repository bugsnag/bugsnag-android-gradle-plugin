package com.bugsnag.android.gradle

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class PluginExtensionTest {

    private Project proj

    @Before
    void setUp() throws Exception {
        proj = ProjectBuilder.builder().build()
        proj.pluginManager.apply 'com.bugsnag.android.gradle'
        proj.pluginManager.apply 'android'
    }

    @Test
    void ensureExtensionDefaults() {
        assertEquals("https://upload.bugsnag.com", proj.bugsnag.endpoint)
        assertTrue(proj.bugsnag.uploadJvmMappings)
        assertTrue(proj.bugsnag.reportBuilds)
        assertFalse(proj.bugsnag.uploadDebugBuildMappings)
        assertFalse(proj.bugsnag.overwrite)
        assertEquals(0, proj.bugsnag.retryCount)
        assertNull(proj.bugsnag.uploadNdkMappings)
        assertNull(proj.bugsnag.sharedObjectPath)
        assertTrue(proj.bugsnag.failOnUploadError)
    }

}
