package com.bugsnag.android.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals

class SmokeTest {

    @Test
    void ensurePluginBuilds() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.bugsnag.android.gradle'
        assertEquals("https://upload.bugsnag.com", project.bugsnag.endpoint)
    }

}
