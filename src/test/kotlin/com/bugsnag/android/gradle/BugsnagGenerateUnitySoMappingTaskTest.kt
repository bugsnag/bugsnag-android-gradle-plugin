package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.BugsnagGenerateUnitySoMappingTask.Companion.isUnitySharedObjectFile
import com.bugsnag.android.gradle.BugsnagGenerateUnitySoMappingTask.Companion.isUnitySymbolsArchive
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BugsnagGenerateUnitySoMappingTaskTest {

    @Test
    fun testIsUnitySymbolsArchive() {
        assertFalse(isUnitySymbolsArchive("", ""))
        assertFalse(isUnitySymbolsArchive("foo", "bar"))
        assertTrue(isUnitySymbolsArchive("symbols.zip", ""))
        assertFalse(isUnitySymbolsArchive("symbols.zip", "example-project"))
        assertFalse(isUnitySymbolsArchive("another-project-symbols.zip", "example-project"))
        assertFalse(isUnitySymbolsArchive("another-project.zip", "example-project"))
        assertTrue(isUnitySymbolsArchive("example-project-symbols.zip", "example-project"))
    }

    @Test
    fun testIsUnitySharedObjectFile() {
        assertFalse(isUnitySharedObjectFile(""))
        assertFalse(isUnitySharedObjectFile("foo"))
        assertFalse(isUnitySharedObjectFile("armeabi-v7a"))
        assertFalse(isUnitySharedObjectFile("x86"))
        assertFalse(isUnitySharedObjectFile("libsomethingelse.sym"))
        assertFalse(isUnitySharedObjectFile("libunity.so"))
        assertTrue(isUnitySharedObjectFile("libil2cpp.sym"))
        assertTrue(isUnitySharedObjectFile("libunity.sym.so"))
        assertTrue(isUnitySharedObjectFile("unity_2019-1.0-v1.symbols/armeabi-v7a/libil2cpp.sym.so"))
    }
}
