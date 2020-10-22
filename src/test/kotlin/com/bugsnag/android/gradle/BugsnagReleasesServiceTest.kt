package com.bugsnag.android.gradle

import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BugsnagReleasesServiceTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var service: BugsnagReleasesService

    @Before
    fun setup() {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = BODY })
            .build()
        service = BugsnagReleasesTask.createService(client)
    }

    @Test
    fun simplePayload() {
        val url = server.url("/")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("Response!")
        )
        val call = service.upload(
            endpoint = url.toString(),
            apiKey = "testKey",
            payload = ReleasePayload(
                buildTool = "gradle",
                apiKey = "testKey",
                appVersion = "1.0",
                appVersionCode = "1",
                metadata = mapOf(
                    "metaKey" to "value"
                ),
                sourceControl = mapOf(
                    "sourceControlKey" to "value"
                ),
                builderName = "builder"
            )
        )

        val response = call.execute()
        check(response.code() == 200)
        check(response.body() == "Response!")

        val recordedBody = server.takeRequest().body.readUtf8()
        assertThat(recordedBody)
            //language=JSON
            .isEqualTo(
                """
                {"buildTool":"gradle","apiKey":"testKey","appVersion":"1.0","appVersionCode":"1","metadata":{"metaKey":"value"},"sourceControl":{"sourceControlKey":"value"},"builderName":"builder"}
                """.trimIndent()
            )
    }
}
