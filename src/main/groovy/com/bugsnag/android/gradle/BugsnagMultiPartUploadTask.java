package com.bugsnag.android.gradle;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
public class BugsnagMultiPartUploadTask extends DefaultTask {

    static final int MAX_RETRY_COUNT = 5;

    String applicationId;
    BaseVariantOutput variantOutput;
    BaseVariant variant;

    void uploadMultipartEntity(MultipartEntity mpEntity) throws IOException, SAXException, ParserConfigurationException {
        AndroidManifestInfo manifestInfo = BugsnagVariantOutputUtils.readManifestFile(getProject(), variant, variantOutput);
        Logger logger = getProject().getLogger();
        BugsnagPluginExtension bugsnag = (BugsnagPluginExtension) getProject().getExtensions().getByName("bugsnag");

        if (manifestInfo.getApiKey() == null || manifestInfo.getApiKey().equals("")) {
            logger.warn("Skipping upload due to invalid parameters");
            if (bugsnag.isFailOnUploadError()) {
                throw new GradleException("Aborting upload due to invalid parameters");
            } else {
                return;
            }
        }

        addPropertiesToMultipartEntity(mpEntity, manifestInfo, bugsnag);

        boolean uploadSuccessful = uploadToServer(mpEntity, bugsnag);

        int maxRetryCount = getRetryCount(bugsnag);
        int retryCount = maxRetryCount;
        while (!uploadSuccessful && retryCount > 0) {
            logger.warn(String.format("Retrying Bugsnag upload (%d/%d) ...",
                maxRetryCount - retryCount + 1, maxRetryCount));
            uploadSuccessful = uploadToServer(mpEntity, bugsnag);
            retryCount--;
        }

        if (!uploadSuccessful && bugsnag.isFailOnUploadError()) {
            throw new GradleException("Upload did not succeed");
        }
    }

    void addPropertiesToMultipartEntity(MultipartEntity mpEntity, AndroidManifestInfo manifestInfo, BugsnagPluginExtension bugsnag) throws UnsupportedEncodingException {
        mpEntity.addPart("apiKey", new StringBody(manifestInfo.getApiKey()));
        mpEntity.addPart("appId", new StringBody(applicationId));
        mpEntity.addPart("versionCode", new StringBody(manifestInfo.getVersionCode()));

        if (manifestInfo.getBuildUUID() != null) {
            mpEntity.addPart("buildUUID", new StringBody(manifestInfo.getBuildUUID()));
        }

        if (manifestInfo.getVersionName() != null) {
            mpEntity.addPart("versionName", new StringBody(manifestInfo.getVersionName()));
        }

        if (bugsnag.isOverwrite()) {
            mpEntity.addPart("overwrite", new StringBody("true"));
        }

        Logger logger = getProject().getLogger();
        logger.debug("apiKey: ${manifestInfo.apiKey}");
        logger.debug("appId: ${applicationId}");
        logger.debug("versionCode: ${manifestInfo.versionCode}");
        logger.debug("buildUUID: ${manifestInfo.buildUUID}");
        logger.debug("versionName: ${manifestInfo.versionName}");
        logger.debug("overwrite: ${project.bugsnag.overwrite}");
    }

    boolean uploadToServer(MultipartEntity mpEntity, BugsnagPluginExtension bugsnag) {
        Logger logger = getProject().getLogger();
        logger.lifecycle("Attempting upload of mapping file to Bugsnag");

        // Make the request
        HttpPost httpPost = new HttpPost(bugsnag.getEndpoint());
        httpPost.setEntity(mpEntity);

        HttpClient httpClient = new DefaultHttpClient();
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, bugsnag.getRequestTimeoutMs());
        HttpConnectionParams.setSoTimeout(params, bugsnag.getRequestTimeoutMs());

        int statusCode;
        String responseEntity;
        try {
            HttpResponse response = httpClient.execute(httpPost);
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            responseEntity = EntityUtils.toString(entity, "utf-8");
        } catch (IOException e) {
            logger.error(String.format("Bugsnag upload failed: %s", e));
            return false;
        } catch (ParseException e) {
            logger.error(String.format("Bugsnag upload failed: %s", e));
            return false;
        }

        if (statusCode == 200) {
            logger.lifecycle("Bugsnag upload successful");
            return true;
        }

        logger.error(String.format("Bugsnag upload failed with code %d: %s", statusCode, responseEntity));
        return false;
    }

    /**
     * Get the retry count defined by the user. If none is set the default is 0 (zero).
     * Also to avoid too much retries the max value is 5 (five).
     *
     * @return the retry count
     */
    int getRetryCount(BugsnagPluginExtension bugsnag) {
        return bugsnag.getRetryCount() >= MAX_RETRY_COUNT ? MAX_RETRY_COUNT : bugsnag.getRetryCount();
    }

}
