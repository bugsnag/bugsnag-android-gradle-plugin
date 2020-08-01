package com.bugsnag.android.gradle

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BugsnagServiceTest {

    @get:Rule
    val server = MockWebServer()

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private lateinit var service: BugsnagService

    @Before
    fun setup() {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = BODY })
            .build()
        service = BugsnagMultiPartUploadRequest.createService(client)
    }

    @Test
    fun simplePayload() {
        val url  = server.url("/")
        server.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("Response!"))

        val manifestInfo = AndroidManifestInfo(
            apiKey = "api key",
            versionCode = "1",
            buildUUID = "lfkajsdflkajdskfhasd",
            versionName = "1.0",
            applicationId = "com.example.test"
        )

        val parts = mutableMapOf<String, RequestBody>()
        parts["apiKey"] = manifestInfo.apiKey.toTextRequestBody()
        parts["appId"] = manifestInfo.applicationId.toTextRequestBody()
        parts["versionCode"] = manifestInfo.versionCode.toTextRequestBody()
        parts["buildUUID"] = manifestInfo.buildUUID.toTextRequestBody()
        parts["versionName"] = manifestInfo.versionName.toTextRequestBody()

        val mappingFile = tmpFolder.newFile()
        mappingFile.writeText("this is totally a mapping file")
        parts["proguard"] = mappingFile.toOctetRequestBody()

        val call = service.uploadFile(
            endpoint = url.toString(),
            parts = parts
        )

        val response = call.execute()
        check(response.code() == 200)
        check(response.body() == "Response!")
    }
}
