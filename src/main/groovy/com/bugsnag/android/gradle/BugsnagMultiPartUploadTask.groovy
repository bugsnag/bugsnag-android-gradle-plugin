package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.ParseException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import org.apache.http.util.EntityUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException

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
class BugsnagMultiPartUploadTask extends DefaultTask {

    static final int MAX_RETRY_COUNT = 5

    String applicationId
    BaseVariantOutput variantOutput;
    BaseVariant variant;

    void uploadMultipartEntity(MultipartEntity mpEntity) {
        AndroidManifestInfo manifestInfo = BugsnagVariantOutputUtils.readManifestFile(project, variant, variantOutput)

        if (manifestInfo.apiKey == null || manifestInfo.apiKey == "") {
            project.logger.warn("Skipping upload due to invalid parameters")
            if (project.bugsnag.failOnUploadError) {
                throw new GradleException("Aborting upload due to invalid parameters")
            } else {
                return
            }
        }

        addPropertiesToMultipartEntity(mpEntity, manifestInfo)

        boolean uploadSuccessful = uploadToServer(mpEntity)

        int maxRetryCount = retryCount
        int retryCount = maxRetryCount
        while (!uploadSuccessful && retryCount > 0) {
            project.logger.warn(String.format("Retrying Bugsnag upload (%d/%d) ...",
                maxRetryCount - retryCount + 1, maxRetryCount))
            uploadSuccessful = uploadToServer(mpEntity)
            retryCount--
        }

        if (!uploadSuccessful && project.bugsnag.failOnUploadError) {
            throw new GradleException("Upload did not succeed")
        }
    }

    void addPropertiesToMultipartEntity(MultipartEntity mpEntity, AndroidManifestInfo manifestInfo) {
        mpEntity.addPart("apiKey", new StringBody(manifestInfo.apiKey))
        mpEntity.addPart("appId", new StringBody(applicationId))
        mpEntity.addPart("versionCode", new StringBody(manifestInfo.versionCode))

        if (manifestInfo.buildUUID != null) {
            mpEntity.addPart("buildUUID", new StringBody(manifestInfo.buildUUID))
        }

        if (manifestInfo.versionName != null) {
            mpEntity.addPart("versionName", new StringBody(manifestInfo.versionName))
        }

        if (project.bugsnag.overwrite || System.properties['bugsnag.overwrite']) {
            mpEntity.addPart("overwrite", new StringBody("true"))
        }

        project.logger.debug("apiKey: ${manifestInfo.apiKey}")
        project.logger.debug("appId: ${applicationId}")
        project.logger.debug("versionCode: ${manifestInfo.versionCode}")
        project.logger.debug("buildUUID: ${manifestInfo.buildUUID}")
        project.logger.debug("versionName: ${manifestInfo.versionName}")
        project.logger.debug("overwrite: ${project.bugsnag.overwrite}")
    }

    boolean uploadToServer(mpEntity) {
        project.logger.lifecycle("Attempting upload of mapping file to Bugsnag")

        // Make the request
        HttpPost httpPost = new HttpPost(project.bugsnag.endpoint)
        httpPost.setEntity(mpEntity)

        HttpClient httpClient = new DefaultHttpClient()
        HttpParams params = httpClient.params
        HttpConnectionParams.setConnectionTimeout(params, project.bugsnag.requestTimeoutMs)
        HttpConnectionParams.setSoTimeout(params, project.bugsnag.requestTimeoutMs)

        int statusCode
        String responseEntity
        try {
            HttpResponse response = httpClient.execute(httpPost)
            statusCode = response.statusLine.statusCode
            HttpEntity entity = response.entity
            responseEntity = EntityUtils.toString(entity, "utf-8")
        } catch (IOException | ParseException e) {
            project.logger.error(String.format("Bugsnag upload failed: %s", e))
            return false
        }

        if (statusCode == 200) {
            project.logger.lifecycle("Bugsnag upload successful")
            return true
        }

        project.logger.error(String.format("Bugsnag upload failed with code %d: %s",
            statusCode, responseEntity))
        false
    }

    /**
     * Get the retry count defined by the user. If none is set the default is 0 (zero).
     * Also to avoid too much retries the max value is 5 (five).
     *
     * @return the retry count
     */
    int getRetryCount() {
        project.bugsnag.retryCount >= MAX_RETRY_COUNT ? MAX_RETRY_COUNT : project.bugsnag.retryCount
    }

}
