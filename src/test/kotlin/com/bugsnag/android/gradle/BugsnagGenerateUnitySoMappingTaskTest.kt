package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.BugsnagGenerateUnitySoMappingTask.Companion.isUnitySharedObjectFile
import com.bugsnag.android.gradle.BugsnagGenerateUnitySoMappingTask.Companion.isUnitySymbolsArchive
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.zip.ZipEntry

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
        assertFalse(isUnitySharedObjectFile(ZipEntry("")))
        assertFalse(isUnitySharedObjectFile(ZipEntry("foo")))
        assertFalse(isUnitySharedObjectFile(ZipEntry("armeabi-v7a")))
        assertFalse(isUnitySharedObjectFile(ZipEntry("x86")))
        assertFalse(isUnitySharedObjectFile(ZipEntry("libsomethingelse.sym")))
        assertTrue(isUnitySharedObjectFile(ZipEntry("libil2cpp.sym")))
        assertTrue(isUnitySharedObjectFile(ZipEntry("libunity.sym.so")))
        assertTrue(isUnitySharedObjectFile(ZipEntry("unity_2019-1.0-v1.symbols/armeabi-v7a/libil2cpp.sym.so")))
    }
}
