package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.LegacyUploadRequestClient
import org.junit.Assert.assertEquals
import org.junit.Test

class UploadRequestClientTest {

    private val info = AndroidManifestInfo(
        "api-key",
        "5",
        "build-uuid-123",
        "1.0",
        "com.example",
        null,
        null
    )

    @Test
    fun testRequestDiffVersionInfo() {
        val client = LegacyUploadRequestClient()
        var requestCount = 0
        val request = {
            requestCount += 1
            ""
        }
        client.makeRequestIfNeeded(info, "{}".hashCode(), request)
        client.makeRequestIfNeeded(info.copy(apiKey = "40fadb0123094f"), "{}".hashCode(), request)
        assertEquals(2, requestCount)
    }

    @Test
    fun testRequestDiffPayload() {
        val client = LegacyUploadRequestClient()
        var requestCount = 0
        val request = {
            requestCount += 1
            ""
        }
        client.makeRequestIfNeeded(info, "{}".hashCode(), request)
        client.makeRequestIfNeeded(info, "[]".hashCode(), request)
        assertEquals(2, requestCount)
    }

    @Test
    fun testRequestSameInfo() {
        val client = LegacyUploadRequestClient()
        var requestCount = 0
        val request = {
            requestCount += 1
            ""
        }
        client.makeRequestIfNeeded(info, "{}".hashCode(), request)
        client.makeRequestIfNeeded(info, "{}".hashCode(), request)
        assertEquals(1, requestCount)
    }
}
