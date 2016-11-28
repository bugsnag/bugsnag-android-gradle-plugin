package com.bugsnag.android.gradle

import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
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
class BugsnagUploadNdkTask extends BugsnagUploadAbstractTask {
    File intermediatePath
    File symbolPath
    String variantName
    File projectDir

    BugsnagUploadNdkTask() {
        super()
        this.description = "Generates and uploads the NDK mapping file(s) to Bugsnag"
    }

    @TaskAction
    def upload() {

        if (intermediatePath == null) {
            return
        }

        // Look for the shared objects in likely folders
        File binariesFile = null;
        for (File intDir : intermediatePath.listFiles()) {
            if (intDir.name.equals("cmake")) { // CMake
                binariesFile = intDir;
            } else if (intDir.name.equals("binaries")) { // Experimental
                binariesFile = intDir;
            } // TODO: where do the build-ndk files get output?
        }

        if (binariesFile == null) {
            project.logger.error("Unable to locate correct path to intermediate binaries under " + intermediatePath.absolutePath)
            return
        }

        // Create the files to upload
        File intermediateBinaries = new File(binariesFile.absolutePath + File.separator + variantName + File.separator + "obj");
        SOMappingFile[] files = SOMappingProcessor.GenerateMappingFiles(intermediateBinaries, symbolPath, projectDir)

        // Read the API key and Build ID etc..
        super.readManifestFile();

        for (SOMappingFile file : files) {
            MultipartEntity mpEntity = new MultipartEntity()
            mpEntity.addPart("soMappingFile", new FileBody(file.mappingFile))
            mpEntity.addPart("arch", new StringBody(file.arch))
            mpEntity.addPart("sharedObjectName", new StringBody(file.sharedObjectName))

            super.uploadMultipartEntity(mpEntity)
        }
    }
}
