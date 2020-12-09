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
            assertNull(uploadReactNativeMappings.orNull)
            assertNull(sourceControl.repository.orNull)
            assertNull(sourceControl.revision.orNull)
            assertNull(sourceControl.provider.orNull)
            assertNull(nodeModulesDir.orNull)
        }

        // ndk/unity upload defaults to false
        val android = proj.extensions.findByType(AppExtension::class.java)!!
        val plugin = proj.plugins.findPlugin(BugsnagPlugin::class.java)!!
        assertFalse(plugin.isUnityLibraryUploadEnabled(bugsnag, android))
        assertFalse(plugin.isNdkUploadEnabled(bugsnag, android))
        assertFalse(plugin.isReactNativeUploadEnabled(bugsnag))
        assertEquals("https://upload.bugsnag.com", plugin.getSourcemapUploadEndpoint(bugsnag))
        assertEquals(emptyList<File>(), plugin.getSharedObjectSearchPaths(proj, bugsnag, android))
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
            uploadReactNativeMappings.set(true)
            metadata.set(mapOf(Pair("test", "a")))
            objdumpPaths.set(mapOf(Pair("armeabi-v7a", "/test/foo")))
            sharedObjectPaths.set(listOf(File("/test/bar")))
            nodeModulesDir.set(File("/test/foo/node_modules"))

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
            assertEquals(File("/test/foo/node_modules"), nodeModulesDir.get())
            assertEquals("https://github.com", sourceControl.repository.get())
            assertEquals("d0e98fc", sourceControl.revision.get())
            assertEquals("github", sourceControl.provider.get())
        }

        // ndk/unity upload overridden to true
        val android = proj.extensions.findByType(AppExtension::class.java)!!
        val plugin = proj.plugins.findPlugin(BugsnagPlugin::class.java)!!
        assertTrue(plugin.isUnityLibraryUploadEnabled(bugsnag, android))
        assertTrue(plugin.isNdkUploadEnabled(bugsnag, android))
        assertTrue(plugin.isReactNativeUploadEnabled(bugsnag))
        val uploadEndpoint = plugin.getSourcemapUploadEndpoint(bugsnag)
        assertEquals("http://localhost:1234/react-native-source-map", uploadEndpoint)
        val expected = listOf(
            File("/test/bar"),
            File(proj.projectDir, "src/main/jniLibs"),
            File(proj.rootDir, "unityLibrary/src/main/jniLibs")
        )
        assertEquals(expected, plugin.getSharedObjectSearchPaths(proj, bugsnag, android))
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
        val expected = listOf(
            File(proj.projectDir, "src/main/jniLibs"),
            File(proj.rootDir, "unityLibrary/src/main/jniLibs")
        )
        assertEquals(expected, plugin.getSharedObjectSearchPaths(proj, bugsnag, android))
    }

    /**
     * Verifies that in a project with the React property extension upload is disabled by default
     */
    @Test
    fun reactNativeUploadHeuristics() {
        val bugsnag = proj.extensions.getByType(BugsnagPluginExtension::class.java)
        val plugin = proj.plugins.findPlugin(BugsnagPlugin::class.java)!!
        proj.extensions.extraProperties.set("react", "some value")
        assertFalse(plugin.isReactNativeUploadEnabled(bugsnag))
    }

    /**
     * Verifies that in a Unity app NDK/Unity uploads are enabled by default
     */
    @Test
    fun unityEnablesNdkUpload() {
        val bugsnag = proj.extensions.getByType(BugsnagPluginExtension::class.java)
        val android = proj.extensions.findByType(AppExtension::class.java)!!
        val plugin = proj.plugins.findPlugin(BugsnagPlugin::class.java)!!
        android.aaptOptions.noCompress.add(".unity3d")

        // ndk/unity uploads overridden to true
        assertTrue(plugin.isUnityLibraryUploadEnabled(bugsnag, android))
        assertTrue(plugin.isNdkUploadEnabled(bugsnag, android))
        val expected = listOf(
            File(proj.projectDir, "src/main/jniLibs"),
            File(proj.rootDir, "unityLibrary/src/main/jniLibs")
        )
        assertEquals(expected, plugin.getSharedObjectSearchPaths(proj, bugsnag, android))
    }
}
