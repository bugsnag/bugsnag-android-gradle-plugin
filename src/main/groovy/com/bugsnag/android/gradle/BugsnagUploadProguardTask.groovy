package com.bugsnag.android.gradle

import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.util.TextUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset
import java.nio.file.Paths

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
class BugsnagUploadProguardTask extends BugsnagMultiPartUploadTask {

    String partName

    BugsnagUploadProguardTask() {
        super()
        this.description = "Uploads the mapping file to Bugsnag"
    }

    @TaskAction
    def upload() {
        File mappingFile = findMappingFile()
        project.logger.info("Using mapping file: $mappingFile")

        // If we haven't enabled proguard for this variant, or the proguard
        // configuration includes -dontobfuscate, the mapping file
        // will not exist (but we also won't need it).
        if (!mappingFile || !mappingFile.exists()) {
            project.logger.warn("Mapping file not found: ${mappingFile}")
            if (project.bugsnag.failOnUploadError) {
                throw new GradleException("Mapping file not found: ${mappingFile}")
            } else {
                return
            }
        }

        // Read the API key and Build ID etc..
        super.readManifestFile()
        project.logger.info("Attempting to upload mapping file: ${mappingFile}")

        // Construct a basic request
        def charset = Charset.forName("UTF-8")
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, charset)
        mpEntity.addPart(partName, new FileBody(mappingFile))

        // Send the request
        super.uploadMultipartEntity(mpEntity)
    }

    private File findMappingFile() {
        if (BugsnagPlugin.hasDexguardPlugin(project) && BugsnagPlugin.hasMultipleOutputs(project)) {
            def mappingFile = findDexguardMappingFile(project)

            if (mappingFile && mappingFile.exists()) {
                return mappingFile
            } else {
                project.logger.warn("Could not find DexGuard mapping file at: $mappingFile -" +
                    " falling back to AGP mapping file value")
            }
        }
        // use AGP supplied value by default, or as fallback
        return variant.mappingFile
    }

    /**
     * Retrieves the location of a DexGuard mapping file for the given variantOutput. The expected location for this
     * is: build/outputs/mapping/<productFlavor>/<buildType>/<split>
     *
     * variant.mappingFile cannot currently be overridden using the AGP DSL on a per-variantOutput basis, which
     * necessitates this workaround. https://issuetracker.google.com/issues/78921539
     */
    File findDexguardMappingFile(Project project) {
        String buildDir = project.buildDir.toString()
        String outputDir = variantOutput.dirName

        if (variantOutput.dirName.endsWith("dpi" + File.separator)) {
            outputDir = new File(variantOutput.dirName).parent

            if (outputDir == null) { // if only density splits enabled
                outputDir = ""
            }
        }
        return Paths.get(buildDir, "outputs", "mapping", variant.dirName, outputDir, "mapping.txt").toFile()
    }

}
