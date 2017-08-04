package com.bugsnag.android.gradle

import groovy.xml.Namespace
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import org.apache.http.util.EntityUtils
import org.gradle.api.DefaultTask
/**
    Task to upload ProGuard mapping files to Bugsnag.

    Reads meta-data tags from the project's AndroidManifest.xml to extract a
    build UUID (injected by BugsnagManifestTask) and a Bugsnag API Key:

    https://developer.android.com/guide/topics/manifest/manifest-intro.html
    https://developer.android.com/guide/topics/manifest/meta-data-element.html

    This task must be called after ProGuard mapping files are generated, so
    it is usually safe to have this be the absolute last task executed during
    a build.
*/
abstract class BugsnagUploadAbstractTask extends DefaultTask {
    static final int MAX_RETRY_COUNT = 5
    static final int TIMEOUT_MILLIS = 60000 // 60 seconds
    String manifestPath
    String applicationId

    // Read from the manifest file
    String apiKey
    String versionCode
    String buildUUID
    String versionName

    BugsnagUploadAbstractTask() {
        super()
    }

    // Read the API key and Build ID etc..
    def readManifestFile() {
        // Parse the AndroidManifest.xml
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
        def xml = new XmlParser().parse(manifestPath)
        def metaDataTags = xml.application['meta-data']

        // Get the Bugsnag API key
        apiKey = getApiKey(metaDataTags, ns)
        if(!apiKey) {
            throw new RuntimeException("Could not find apiKey in '$BugsnagPlugin.API_KEY_TAG' <meta-data> tag in your AndroidManifest.xml or in your gradle config")
        }

        // Get the build version
        versionCode = xml.attributes()[ns.versionCode]
        if (versionCode == null) {
            project.logger.warn("Could not find 'android:versionCode' value in your AndroidManifest.xml")
            return
        }

        // Uniquely identify the build so that we can identify the proguard file.
        buildUUID = getBuildUUID(metaDataTags, ns)

        // Get the version name
        versionName = xml.attributes()[ns.versionName]
    }

    def uploadMultipartEntity(MultipartEntity mpEntity) {
        addPropertiesToMultipartEntity(mpEntity)

        boolean uploadSuccessful = uploadToServer(mpEntity)

        def maxRetryCount = getRetryCount()
        def retryCount = maxRetryCount
        while (!uploadSuccessful && retryCount > 0) {
            project.logger.warn(String.format("Retrying Bugsnag upload (%d/%d) ...",
                                maxRetryCount - retryCount + 1, maxRetryCount))
            uploadSuccessful = uploadToServer(mpEntity)
            retryCount--
        }
    }

    def addPropertiesToMultipartEntity(MultipartEntity mpEntity) {
        mpEntity.addPart("apiKey", new StringBody(apiKey))
        mpEntity.addPart("appId", new StringBody(applicationId))
        mpEntity.addPart("versionCode", new StringBody(versionCode))

        if(buildUUID != null) {
            mpEntity.addPart("buildUUID", new StringBody(buildUUID));
        }

        if (versionName != null) {
            mpEntity.addPart("versionName", new StringBody(versionName))
        }

        if (project.bugsnag.overwrite || System.properties['bugsnag.overwrite']) {
            mpEntity.addPart("overwrite", new StringBody("true"))
        }
    }

    def boolean uploadToServer(mpEntity) {
        println("Attempting upload to Bugsnag")

        // Make the request
        HttpPost httpPost = new HttpPost(project.bugsnag.endpoint)
        httpPost.setEntity(mpEntity);

        HttpClient httpClient = new DefaultHttpClient()
        HttpParams params = httpClient.getParams()
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_MILLIS)
        HttpConnectionParams.setSoTimeout(params, TIMEOUT_MILLIS)

        int statusCode = 0
        def responseEntity = null
        try {
            HttpResponse response = httpClient.execute(httpPost)
            statusCode = response.getStatusLine().getStatusCode()
            responseEntity = EntityUtils.toString(response.getEntity(), "utf-8")
        } catch (Exception e) {
            project.logger.warn(String.format("Bugsnag upload failed: %s", e))
            return false
        }

        if (statusCode == 200) {
            println("Bugsnag upload succeeded")
            return true
        }

        project.logger.warn(String.format("Bugsnag upload failed with code %d: %s",
                            statusCode, responseEntity))
        return false
    }

    def getApiKey(metaDataTags, ns) {
        def apiKey = null

        if(project.bugsnag.apiKey != null) {
            apiKey = project.bugsnag.apiKey
        } else {
            def apiKeyTags = metaDataTags.findAll{ it.attributes()[ns.name].equals(BugsnagPlugin.API_KEY_TAG) }
            if (apiKeyTags.size() > 0) {
                apiKey = apiKeyTags[0].attributes()[ns.value]
            }
        }

        return apiKey;
    }

    def getBuildUUID(metaDataTags, ns) {
        def buildUUID = null

        def buildUUIDTags = metaDataTags.findAll{ it.attributes()[ns.name].equals(BugsnagPlugin.BUILD_UUID_TAG) }
        if (buildUUIDTags.size() == 0) {
            project.logger.warn("Could not find '$BugsnagPlugin.BUILD_UUID_TAG' <meta-data> tag in your AndroidManifest.xml")
        } else {
            buildUUID = buildUUIDTags[0].attributes()[ns.value]
        }

        return buildUUID
    }

    /**
     * Get the retry count defined by the user. If none is set the default is 0 (zero).
     * Also to avoid too much retries the max value is 5 (five).
     *
     * @return the retry count
     */
    def getRetryCount() {
        return project.bugsnag.retryCount >= MAX_RETRY_COUNT ? MAX_RETRY_COUNT : project.bugsnag.retryCount
    }

}
