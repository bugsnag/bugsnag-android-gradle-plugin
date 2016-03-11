package com.bugsnag.android.gradle

import groovy.util.XmlParser
import groovy.xml.Namespace

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class BugsnagUploadTask extends DefaultTask {
    String manifestPath
    String applicationId
    File mappingFile

    BugsnagUploadTask() {
        super()
        this.description = "Uploads the proguard mapping file to Bugsnag"
    }

    @TaskAction
    def upload() {
        // Parse the AndroidManifest.xml
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
        def xml = new XmlParser().parse(manifestPath)

        // Uniquely identify the build so that we can identify the proguard file.
        def buildUUID =  ""
        // Get the Bugsnag API key
        def apiKey
        if(project.bugsnag.apiKey != null) {
            apiKey = project.bugsnag.apiKey
        } else {
            def metaDataTags = xml.application['meta-data']
            def apiKeyTags = metaDataTags.findAll{ it.attributes()[ns.name].equals(BugsnagPlugin.API_KEY_TAG) }
            if (apiKeyTags.size() == 0) {
                project.logger.warn("Could not find '$BugsnagPlugin.API_KEY_TAG' <meta-data> tag in your AndroidManifest.xml")
                return
            }
            apiKey = apiKeyTags[0].attributes()[ns.value]

            def buildUUIDTags = metaDataTags.findAll{ it.attributes()[ns.name].equals(BugsnagPlugin.BUILD_UUID_TAG) }
            if (buildUUIDTags.size() == 0) {
                project.logger.warn("Could not find '$BugsnagPlugin.BUILD_UUID_TAG' <meta-data> tag in your AndroidManifest.xml")
            } else {
                buildUUID = buildUUIDTags[0].attributes()[ns.value]
            }
        }

        // Get the build version
        def versionName = xml.attributes()[ns.versionName]
        def versionCode = xml.attributes()[ns.versionCode]
        if (versionCode == null) {
            project.logger.warn("Could not find 'android:versionCode' value in your AndroidManifest.xml")
            return
        }

        // If proguard configuration includes -dontobfuscate, the mapping file
        // will not exist (but we also won't need it).
        if (!mappingFile.exists()) {
            return
        }

        // Upload the mapping file to Bugsnag
        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("proguard", new FileBody(mappingFile));
        mpEntity.addPart("apiKey", new StringBody(apiKey));
        mpEntity.addPart("appId", new StringBody(applicationId));
        mpEntity.addPart("versionCode", new StringBody(versionCode));
        mpEntity.addPart("buildUUID", new StringBody(buildUUID));

        if (versionName != null) {
            mpEntity.addPart("versionName", new StringBody(versionName));
        }
        if (System.properties['bugsnag.overwrite']) {
            mpEntity.addPart("overwrite", new StringBody("true"));
        }

        HttpPost httpPost = new HttpPost(project.bugsnag.endpoint)
        httpPost.setEntity(mpEntity);

        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(httpPost);

        if (response.getStatusLine().getStatusCode() != 200) {
            project.logger.warn("Bugsnag upload failed: " + EntityUtils.toString(response.getEntity(), "utf-8"))
        }
    }
}
