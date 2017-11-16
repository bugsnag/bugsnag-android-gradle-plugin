package com.bugsnag.android.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertNull

class SmokeTest {

    @Test
    void ensurePluginBuilds() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.bugsnag.android.gradle'

        assertEquals("https://upload.bugsnag.com", project.bugsnag.endpoint)
        assertNull(project.bugsnag.apiKey)
        assertTrue(project.bugsnag.autoUpload)
        assertTrue(project.bugsnag.autoProguardConfig)
        assertFalse(project.bugsnag.uploadDebugBuildMappings)
        assertFalse(project.bugsnag.overwrite)
        assertEquals(0, project.bugsnag.retryCount)
        assertFalse(project.bugsnag.ndk)
        assertNull(project.bugsnag.sharedObjectPath)
    }

}
