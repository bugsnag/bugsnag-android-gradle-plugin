package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.ProgressInterceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.UUID

class BugsnagMultiPartUploadRequestTest {

    @get:Rule
    val server = MockWebServer()

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private lateinit var request: BugsnagMultiPartUploadRequest

    @Before
    fun setup() {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = BODY })
            .addInterceptor(ProgressInterceptor())
            .build()
        request = BugsnagMultiPartUploadRequest(
            failOnUploadError = true,
            overwrite = true,
            endpoint = server.url("/").toString(),
            okHttpClient = client
        )
    }

    @Test
    fun simplePayload() {
        server.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("Response!"))
        val fakeMappingFile = tmpFolder.newFile("mapping.txt").apply {
            writeText("Here's some super real mapping content")
        }
        request.uploadMultipartEntity(
            AndroidManifestInfo(
                "alskdjfasd",
                "10",
                UUID.randomUUID().toString(),
                "1.0",
                "com.example"
            )
        , 1) {
            it.addFormDataPart("proguard", fakeMappingFile.name, fakeMappingFile.asRequestBody())
        }
    }
}
