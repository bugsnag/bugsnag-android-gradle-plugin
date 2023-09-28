package com.bugsnag.android.gradle.internal

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.builder.model.BuildType
import com.bugsnag.android.gradle.GroovyCompat
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class DexguardCompatKtTest {

    @Mock
    lateinit var proj: Project

    @Mock
    lateinit var pluginManager: PluginManager

    @Mock
    lateinit var extensions: ExtensionContainer

    @Mock
    lateinit var variant: ApkVariant

    @Mock
    lateinit var variantOutput: ApkVariantOutput

    @Mock
    lateinit var buildType: BuildType

    @Before
    fun setUp() {
        `when`(proj.pluginManager).thenReturn(pluginManager)
        `when`(proj.extensions).thenReturn(extensions)
        `when`(proj.buildDir).thenReturn(File("/build-dir"))
        `when`(variant.buildType).thenReturn(buildType)
    }

    @Test
    fun hasDexguardPlugin() {
        `when`(pluginManager.hasPlugin("dexguard")).thenReturn(false)
        assertFalse(proj.hasDexguardPlugin())

        `when`(pluginManager.hasPlugin("dexguard")).thenReturn(true)
        assertTrue(proj.hasDexguardPlugin())
    }

    @Test
    fun dexguardGetVersionNull() {
        // handles when dexguard is not applied
        `when`(extensions.findByName("dexguard")).thenReturn(null)
        assertNull(GroovyCompat.getDexguardVersionString(proj))
    }

    @Test
    fun dexguardGetFromVersion() {
        // dexguard.version set
        `when`(extensions.findByName("dexguard")).thenReturn(mapOf(Pair("version", "8.7.02")))
        assertEquals("9.0.0", GroovyCompat.getDexguardVersionString(proj))
    }

    @Test
    fun dexguardMajorVersionNull() {
        // handles when dexguard is not applied
        `when`(extensions.findByName("dexguard")).thenReturn(null)
        assertEquals(9, getDexguardMajorVersionInt(proj))
    }

    @Test
    fun dexguardMajorFromVersion() {
        // dexguard.version set
        `when`(extensions.findByName("dexguard")).thenReturn(mapOf(Pair("version", "8.7.02")))
        assertEquals(9, getDexguardMajorVersionInt(proj))
    }

    @Test
    fun testFindMappingFileDexguard9() {
        // default APK build
        `when`(variant.dirName).thenReturn(File("release").path)
        `when`(variantOutput.dirName).thenReturn("")
        val defaultMappingFiles = findMappingFileDexguard9(proj, variant, variantOutput)
        assertEquals(File("/build-dir/outputs/dexguard/mapping/apk/release/mapping.txt"), defaultMappingFiles[0])
        assertEquals(File("/build-dir/outputs/dexguard/mapping/apk/release/mapping.txt"), defaultMappingFiles[0])

        // ABI split
        `when`(variant.dirName).thenReturn(File("release").path)
        `when`(variantOutput.dirName).thenReturn("x86")
        val abiSplitApk = File("/build-dir/outputs/dexguard/mapping/apk/release/x86/mapping.txt")
        assertEquals(abiSplitApk, findMappingFileDexguard9(proj, variant, variantOutput)[0])

        // productFlavor
        `when`(variant.dirName).thenReturn(File("demo/full").path)
        `when`(variantOutput.dirName).thenReturn("")
        val productFlavorApk = File("/build-dir/outputs/dexguard/mapping/apk/demo/full/mapping.txt")
        assertEquals(productFlavorApk, findMappingFileDexguard9(proj, variant, variantOutput)[0])

        // productFlavor split
        `when`(variant.dirName).thenReturn(File("demo/full").path)
        `when`(variantOutput.dirName).thenReturn("x86/hdpi")
        val productFlavorSplitApk = File("/build-dir/outputs/dexguard/mapping/apk/demo/full/x86/hdpi/mapping.txt")
        assertEquals(productFlavorSplitApk, findMappingFileDexguard9(proj, variant, variantOutput)[0])
    }

    @Test
    fun testFindMappingFileDexguardLegacy() {
        // default APK build
        `when`(variant.dirName).thenReturn(File("release").path)
        `when`(variantOutput.dirName).thenReturn("")
        val defaultApk = File("/build-dir/outputs/mapping/release/mapping.txt")
        assertEquals(defaultApk, findMappingFileDexguardLegacy(proj, variant, variantOutput))

        // ABI split
        `when`(variant.dirName).thenReturn(File("release").path)
        `when`(variantOutput.dirName).thenReturn("x86")
        val abiSplitApk = File("/build-dir/outputs/mapping/release/x86/mapping.txt")
        assertEquals(abiSplitApk, findMappingFileDexguardLegacy(proj, variant, variantOutput))

        // productFlavor
        `when`(variant.dirName).thenReturn(File("demo/full").path)
        `when`(variantOutput.dirName).thenReturn("")
        val productFlavorApk = File("/build-dir/outputs/mapping/demo/full/mapping.txt")
        assertEquals(productFlavorApk, findMappingFileDexguardLegacy(proj, variant, variantOutput))

        // productFlavor split
        `when`(variant.dirName).thenReturn(File("demo/full").path)
        `when`(variantOutput.dirName).thenReturn("x86/hdpi")
        val productFlavorSplitApk = File("/build-dir/outputs/mapping/demo/full/x86/hdpi/mapping.txt")
        assertEquals(productFlavorSplitApk, findMappingFileDexguardLegacy(proj, variant, variantOutput))
    }

    @Test
    fun testDexguardAabTask() {
        // default aab task name
        `when`(variant.flavorName).thenReturn("")
        `when`(buildType.name).thenReturn("release")
        assertEquals("dexguardAabRelease", getDexguardAabTaskName(variant))

        // productFlavor aab task name
        `when`(variant.flavorName).thenReturn("fish")
        `when`(buildType.name).thenReturn("demo")
        assertEquals("dexguardAabFishDemo", getDexguardAabTaskName(variant))
    }
}
