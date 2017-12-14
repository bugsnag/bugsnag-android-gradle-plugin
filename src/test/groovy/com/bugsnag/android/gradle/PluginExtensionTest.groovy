package com.bugsnag.android.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

class PluginExtensionTest {

    private Project proj

    @Before
    void setUp() throws Exception {
        proj = ProjectBuilder.builder().build()
        proj.pluginManager.apply 'com.bugsnag.android.gradle'
    }

    @Test
    void ensureExtensionDefaults() {
        assertEquals("https://upload.bugsnag.com", proj.bugsnag.endpoint)
        assertNull(proj.bugsnag.apiKey)
        assertTrue(proj.bugsnag.autoUpload)
        assertTrue(proj.bugsnag.autoProguardConfig)
        assertFalse(proj.bugsnag.uploadDebugBuildMappings)
        assertFalse(proj.bugsnag.overwrite)
        assertEquals(0, proj.bugsnag.retryCount)
        assertNull(proj.bugsnag.ndk)
        assertNull(proj.bugsnag.sharedObjectPath)
    }


}
