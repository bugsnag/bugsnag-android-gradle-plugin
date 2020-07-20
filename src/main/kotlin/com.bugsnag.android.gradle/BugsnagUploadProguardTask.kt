package com.bugsnag.android.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Paths
import javax.inject.Inject

/**
 * Task to upload ProGuard mapping files to Bugsnag.
 *
 * Reads meta-data tags from the project's AndroidManifest.xml to extract a
 * build UUID (injected by BugsnagManifestTask) and a Bugsnag API Key:
 *
 * https://developer.android.com/guide/topics/manifest/manifest-intro.html
 * https://developer.android.com/guide/topics/manifest/meta-data-element.html
 *
 * This task must be called after ProGuard mapping files are generated, so
 * it is usually safe to have this be the absolute last task executed during
 * a build.
 */
open class BugsnagUploadProguardTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Uploads the mapping file to Bugsnag"
    }

    @Internal
    lateinit var variantOutput: ApkVariantOutput

    @Internal
    lateinit var variant: ApkVariant

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun upload() {
        val mappingFile = findMappingFile(project)
        val logger = project.logger
        logger.info("Using mapping file: \$mappingFile")

        // If we haven't enabled proguard for this variant, or the proguard
        // configuration includes -dontobfuscate, the mapping file
        // will not exist (but we also won't need it).
        if (mappingFile == null || !mappingFile.exists()) {
            logger.warn("Mapping file not found: \${mappingFile}")
            val bugsnag = project.extensions.findByType(BugsnagPluginExtension::class.java)!!
            if (bugsnag.isFailOnUploadError) {
                throw GradleException("Mapping file not found: \${mappingFile}")
            } else {
                return
            }
        } else if (mappingFile.length() == 0L) { // proguard's -dontobfuscate generates an empty mapping file
            return
        }

        // Read the API key and Build ID etc..
        logger.info("Attempting to upload mapping file: \${mappingFile}")

        // Construct a basic request
        val charset = Charset.forName("UTF-8")
        val mpEntity = MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, charset)
        mpEntity.addPart("proguard", FileBody(mappingFile))

        // Send the request
        val request = BugsnagMultiPartUploadRequest()
        request.variant = variant
        request.variantOutput = variantOutput
        request.uploadMultipartEntity(project, mpEntity, parseManifestInfo())
    }

    private fun findMappingFile(project: Project): File? {
        val plugin = project.plugins.getPlugin(BugsnagPlugin::class.java)
        val android = project.extensions.getByType(AppExtension::class.java)
        if (plugin.hasDexguardPlugin(project) && plugin.hasMultipleOutputs(android)) {
            val mappingFile = findDexguardMappingFile(project)
            if (mappingFile.exists()) {
                return mappingFile
            } else {
                project.logger.warn("Could not find DexGuard mapping file at: \$mappingFile -" +
                    " falling back to AGP mapping file value")
            }
        }

        // Use AGP supplied value, preferring the new "getMappingFileProvider" API but falling back
        // to the old "mappingFile" API if necessary
        return try {
            val mappingFileProvider = variant.mappingFileProvider.orNull

            // We will warn about not finding a mapping file later, so there's no need to warn here
            if (mappingFileProvider == null || mappingFileProvider.isEmpty) {
                null
            } else mappingFileProvider.singleFile
        } catch (exc: Throwable) {
            variant.mappingFile
        }
    }

    /**
     * Retrieves the location of a DexGuard mapping file for the given variantOutput. The expected location for this
     * is: build/outputs/mapping/<productFlavor>/<buildType>/<split>
     *
     * variant.mappingFile cannot currently be overridden using the AGP DSL on a per-variantOutput basis, which
     * necessitates this workaround. https://issuetracker.google.com/issues/78921539
     */
    private fun findDexguardMappingFile(project: Project): File {
        val buildDir = project.buildDir.toString()
        var outputDir = variantOutput.dirName
        if (variantOutput.dirName.endsWith("dpi" + File.separator)) {
            outputDir = File(variantOutput.dirName).parent
            if (outputDir == null) { // if only density splits enabled
                outputDir = ""
            }
        }
        return Paths.get(buildDir, "outputs", "mapping", variant.dirName, outputDir, "mapping.txt").toFile()
    }
}
