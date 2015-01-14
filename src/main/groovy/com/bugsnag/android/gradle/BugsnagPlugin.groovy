package com.bugsnag.android.gradle

import java.io.File

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

import org.gradle.api.Plugin
import org.gradle.api.Project

class BugsnagPlugin implements Plugin<Project> {
    private static final String API_KEY_TAG = 'com.bugsnag.android.API_KEY'

    void apply(Project project) {
        project.extensions.create("bugsnag", BugsnagPluginExtension)

        project.android.applicationVariants.all { variant ->

            // Only create Bugsnag tasks for proguard-enabled variants
            if (variant.getObfuscation() == null) {
                return
            }

            // Create Bugsnag post-build task
            def variantName = variant.name.capitalize()
            def bugsnagTask = project.task("uploadBugsnag${variantName}Mapping") << {
                // Find the processed manifest for this variant
                def manifestPath = variant.outputs[0].processManifest.manifestOutputFile

                def appId = variant.applicationId

                // Parse the AndroidManifest.xml
                def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
                def xml = new XmlParser().parse(manifestPath)

                // Get the Bugsnag API key
                def apiKey
                if(project.bugsnag.apiKey != null) {
                    apiKey = project.bugsnag.apiKey
                } else {
                    def metaDataTags = xml.application['meta-data']
                    def apiKeyTags = metaDataTags.findAll{ it.attributes()[ns.name].equals(API_KEY_TAG) }
                    if (apiKeyTags.size() == 0) {
                        project.logger.warn("Could not find '$API_KEY_TAG' <meta-data> tag in your AndroidManifest.xml")
                        return
                    }
                    apiKey = apiKeyTags[0].attributes()[ns.value]
                }

                // Get the build version
                def versionName = xml.attributes()[ns.versionName]
                def versionCode = xml.attributes()[ns.versionCode]
                if (versionCode == null) {
                    project.logger.warn("Could not find 'android:versionCode' value in your AndroidManifest.xml")
                    return
                }

                // Find the Proguard mapping file
                File mappingFile = variant.getMappingFile()

                // If proguard configuration includes -dontobfuscate, the mapping file
                // will not exist (but we also won't need it).
                if (!mappingFile.exists()) {
                    return
                }

                // Upload the mapping file to Bugsnag
                MultipartEntity mpEntity = new MultipartEntity();
                mpEntity.addPart("proguard", new FileBody(mappingFile));
                mpEntity.addPart("apiKey", new StringBody(apiKey));
                mpEntity.addPart("appId", new StringBody(appId));
                mpEntity.addPart("versionCode", new StringBody(versionCode));

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

            // Run Bugsnag post-build tasks as part of a build
            project.tasks["package${variantName}"].dependsOn bugsnagTask
            bugsnagTask.dependsOn project.tasks["proguard${variantName}"]
        }
    }
}


