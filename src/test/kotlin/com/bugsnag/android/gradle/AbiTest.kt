package com.bugsnag.android.gradle

import org.junit.Assert.*
import org.junit.Test

class AbiTest {

    @Test
    fun findExisting() {
        val obs = Abi.findByName(Abi.ARM64_V8A.abiName)
        assertEquals(Abi.ARM64_V8A, obs)
    }

    @Test
    fun findNonExisting() {
        val obs = Abi.findByName("foo")
        assertNull(obs)
    }
}
