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
        proj.pluginManager.apply 'android'
    }

    @Test
    void ensureExtensionDefaults() {
        assertEquals("https://upload.bugsnag.com", proj.bugsnag.endpoint)
        assertNull(proj.bugsnag.apiKey)
        assertTrue(proj.bugsnag.autoUpload)
        assertTrue(proj.bugsnag.autoReportBuilds)
        assertTrue(proj.bugsnag.autoProguardConfig)
        assertFalse(proj.bugsnag.uploadDebugBuildMappings)
        assertFalse(proj.bugsnag.overwrite)
        assertEquals(0, proj.bugsnag.retryCount)
        assertFalse(proj.bugsnag.ndk)
        assertNull(proj.bugsnag.sharedObjectPath)
        assertFalse(BugsnagPlugin.hasDexguardPlugin(proj))
        assertTrue(proj.bugsnag.failOnUploadError)

        assertFalse(BugsnagPlugin.hasMultipleOutputs(proj))

    }

}
