package com.bugsnag.android.gradle.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class BugsnagHttpClientHelperKtTest {

    @Test

    fun testRunRequestWithRetries() {
        var attempts = 0

        try {
            runRequestWithRetries(5) {
                attempts++
                throw RuntimeException()
            }
            fail("Expected exception to be thrown after max retries reached")
        } catch (exc: Throwable) {
            assertEquals(5, attempts)
        }
    }

    @Test
    fun testRunRequestNoFailure() {
        var attempts = 0
        runRequestWithRetries(5) {
            attempts++
            ""
        }
        assertEquals(1, attempts)
    }

    @Test
    fun testRunRequestZeroRetries() {
        var attempts = 0
        runRequestWithRetries(0) {
            attempts++
            ""
        }
        assertEquals(1, attempts)
    }
}
