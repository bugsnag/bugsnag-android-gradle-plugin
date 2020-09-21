package com.bugsnag.android.gradle

import com.android.build.gradle.AppExtension
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
            assertNull(uploadNdkUnityLibraryMappings.orNull)
            assertNull(sourceControl.repository.orNull)
            assertNull(sourceControl.revision.orNull)
            assertNull(sourceControl.provider.orNull)
        }

        // ndk/unity upload defaults to false
        val android = proj.extensions.findByType(AppExtension::class.java)!!
        val plugin = proj.plugins.findPlugin(BugsnagPlugin::class.java)!!
        assertFalse(plugin.isUnityLibraryUploadEnabled(bugsnag, android))
        assertFalse(plugin.isNdkUploadEnabled(bugsnag, android))
    }

    /**
     * Verifies that the default extension values can be overridden
     */
    @Test
    fun overrideExtensionValues() {
        val bugsnag = proj.extensions.getByType(BugsnagPluginExtension::class.java)

        // override default values
        with(bugsnag) {
            builderName.set("Joe Bloggs")
            enabled.set(false)
            failOnUploadError.set(false)
            overwrite.set(true)
            reportBuilds.set(false)
            projectRoot.set("/users/foo")
            endpoint.set("http://localhost:1234")
            releasesEndpoint.set("http://localhost:5678")
            retryCount.set(2)
            requestTimeoutMs.set(10000)
            uploadJvmMappings.set(false)
            uploadNdkMappings.set(true)
            uploadNdkUnityLibraryMappings.set(true)
            metadata.set(mapOf(Pair("test", "a")))
            objdumpPaths.set(mapOf(Pair("armeabi-v7a", "/test/foo")))
            sharedObjectPaths.set(listOf(File("/test/bar")))

            sourceControl.repository.set("https://github.com")
            sourceControl.revision.set("d0e98fc")
            sourceControl.provider.set("github")
        }

        with(bugsnag) {
            assertEquals("Joe Bloggs", builderName.get())
            assertFalse(enabled.get())
            assertFalse(failOnUploadError.get())
            assertTrue(overwrite.get())
            assertFalse(reportBuilds.get())
            assertEquals("/users/foo", projectRoot.get())
            assertEquals("http://localhost:1234", endpoint.get())
            assertEquals("http://localhost:5678", releasesEndpoint.get())
            assertEquals(2, retryCount.get())
            assertEquals(10000, requestTimeoutMs.get())
            assertFalse(uploadJvmMappings.get())
            assertTrue(uploadNdkMappings.get())
            assertTrue(uploadNdkUnityLibraryMappings.get())
            assertEquals(mapOf(Pair("test", "a")), metadata.get())
            assertEquals(mapOf(Pair("armeabi-v7a", "/test/foo")), objdumpPaths.get())
            assertEquals(listOf(File("/test/bar")), sharedObjectPaths.get())
            assertEquals("https://github.com", sourceControl.repository.get())
            assertEquals("d0e98fc", sourceControl.revision.get())
            assertEquals("github", sourceControl.provider.get())
        }

        // ndk/unity upload overridden to true
        val android = proj.extensions.findByType(AppExtension::class.java)!!
        val plugin = proj.plugins.findPlugin(BugsnagPlugin::class.java)!!
        assertTrue(plugin.isUnityLibraryUploadEnabled(bugsnag, android))
        assertTrue(plugin.isNdkUploadEnabled(bugsnag, android))
    }

    /**
     * Verifies that the NDK/Unity heuristics control whether tasks are created
     */
    @Test
    fun uploadHeuristics() {
        val bugsnag = proj.extensions.getByType(BugsnagPluginExtension::class.java)
        val android = proj.extensions.findByType(AppExtension::class.java)!!
        val plugin = proj.plugins.findPlugin(BugsnagPlugin::class.java)!!

        // used to check
        android.aaptOptions.noCompress.add(".unity3d")
        android.externalNativeBuild.cmake.path = File("/users/sdk/cmake")

        // ndk/unity upload overridden to true
        assertTrue(plugin.isUnityLibraryUploadEnabled(bugsnag, android))
        assertTrue(plugin.isNdkUploadEnabled(bugsnag, android))
    }
}
