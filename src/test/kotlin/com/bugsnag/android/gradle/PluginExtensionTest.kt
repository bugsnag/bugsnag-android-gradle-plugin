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

        with(bugsnag) {
            assertNull(builderName.orNull)
            assertTrue(enabled.get())
            assertEquals("https://upload.bugsnag.com", endpoint.get())
            assertTrue(failOnUploadError.get())
            assertEquals(emptyMap<String, String>(), metadata.get())
            assertEquals(emptyMap<String, String>(), objdumpPaths.get())
            assertFalse(overwrite.get())
            assertNull(projectRoot.orNull)
            assertEquals("https://build.bugsnag.com", releasesEndpoint.get())
            assertTrue(reportBuilds.get())
            assertEquals(60000, requestTimeoutMs.get())
            assertEquals(0, retryCount.get())
            assertEquals(emptyList<File>(), sharedObjectPaths.get())
            assertTrue(uploadJvmMappings.get())
            assertNull(uploadNdkMappings.orNull)
            assertNull(sourceControl.repository.orNull)
            assertNull(sourceControl.revision.orNull)
            assertNull(sourceControl.provider.orNull)
        }
    }
}
