package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.SharedObjectMappingFileFactory.filterUnitySoLines
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedObjectMappingFileFactoryTest {

    @Test
    fun testUnityLineFilter() {
        assertFalse(filterUnitySoLines(""))
        assertFalse(filterUnitySoLines("Some useless metadata"))
        assertTrue(filterUnitySoLines("SYMBOL TABLE:"))
        assertTrue(filterUnitySoLines(" F "))
        assertFalse(filterUnitySoLines("SYMBOL TABLE: *UND*"))
        assertFalse(filterUnitySoLines(" F *UND*"))
        assertTrue(filterUnitySoLines("0004e618 l     F .text 00000010 __on_dlclose"))
        assertFalse(filterUnitySoLines("F *UND* 00000000 __system_property_read00000000"))
    }
}
