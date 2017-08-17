package com.bugsnag.android.gradle

import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.gradle.api.tasks.TaskAction
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
class BugsnagUploadProguardTask extends BugsnagUploadAbstractTask {
    File mappingFile

    BugsnagUploadProguardTask() {
        super()
        this.description = "Uploads the proguard mapping file to Bugsnag"
    }

    @TaskAction
    def upload() {
        // If we haven't enabled proguard for this variant, or the proguard
        // configuration includes -dontobfuscate, the mapping file
        // will not exist (but we also won't need it).
        if (!mappingFile || !mappingFile.exists()) {
            project.logger.info("Mapping file not found for Proguard")
            return
        }

        // Read the API key and Build ID etc..
        super.readManifestFile();
        project.logger.info("Attempting to upload proguard mapping file: " + mappingFile)

        // Construct a basic request
        MultipartEntity mpEntity = new MultipartEntity()
        mpEntity.addPart("proguard", new FileBody(mappingFile))

        // Send the request
        super.uploadMultipartEntity(mpEntity)
    }
}
