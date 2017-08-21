package com.bugsnag.android.gradle

import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.gradle.api.tasks.TaskAction

/**

 Deprecated by google: http://tools.android.com/tech-docs/jackandjill

 Task to upload Jack mapping files to Bugsnag.

 Reads meta-data tags from the project's AndroidManifest.xml to extract a
 build UUID (injected by BugsnagManifestTask) and a Bugsnag API Key:

 https://developer.android.com/guide/topics/manifest/manifest-intro.html
 https://developer.android.com/guide/topics/manifest/meta-data-element.html

 This task must be called after ProGuard mapping files are generated, so
 it is usually safe to have this be the absolute last task executed during
 a build.
 */
@Deprecated
class BugsnagUploadJackTask extends BugsnagUploadAbstractTask {
    File mappingFile

    BugsnagUploadJackTask() {
        super()
        this.description = "Uploads the jack mapping file to Bugsnag"
    }

    @TaskAction
    def upload() {
        // If we haven't enabled jack for this variant, or the proguard
        // configuration includes -dontobfuscate, the mapping file
        // will not exist (but we also won't need it).
        if (!mappingFile || !mappingFile.exists()) {
            project.logger.info("Mapping file not found for Jack")
            return
        }

        // Read the API key and Build ID etc..
        super.readManifestFile();

        // Construct a basic request
        MultipartEntity mpEntity = new MultipartEntity()
        mpEntity.addPart("jack", new FileBody(mappingFile))

        // Send the request
        super.uploadMultipartEntity(mpEntity)
    }
}
