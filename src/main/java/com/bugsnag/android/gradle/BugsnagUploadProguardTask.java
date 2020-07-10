package com.bugsnag.android.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

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
public class BugsnagUploadProguardTask extends DefaultTask {

    String partName;
    String applicationId;
    BaseVariantOutput variantOutput;
    BaseVariant variant;

    @TaskAction
    void upload() throws IOException, SAXException, ParserConfigurationException {
        Project project = getProject();
        File mappingFile = findMappingFile(project);
        Logger logger = project.getLogger();
        logger.info("Using mapping file: $mappingFile");

        // If we haven't enabled proguard for this variant, or the proguard
        // configuration includes -dontobfuscate, the mapping file
        // will not exist (but we also won't need it).
        if (mappingFile == null || !mappingFile.exists()) {
            logger.warn("Mapping file not found: ${mappingFile}");
            BugsnagPluginExtension bugsnag = project.getExtensions().findByType(BugsnagPluginExtension.class);
            if (bugsnag.isFailOnUploadError()) {
                throw new GradleException("Mapping file not found: ${mappingFile}");
            } else {
                return;
            }
        } else if (mappingFile.length() == 0) { // proguard's -dontobfuscate generates an empty mapping file
            return;
        }

        // Read the API key and Build ID etc..
        BugsnagVariantOutputUtils.readManifestFile(project, variant, variantOutput);
        logger.info("Attempting to upload mapping file: ${mappingFile}");

        // Construct a basic request
        Charset charset = Charset.forName("UTF-8");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, charset);
        mpEntity.addPart(partName, new FileBody(mappingFile));

        // Send the request
        BugsnagMultiPartUploadRequest request = new BugsnagMultiPartUploadRequest();
        request.applicationId = applicationId;
        request.variant = variant;
        request.variantOutput = variantOutput;
        request.uploadMultipartEntity(mpEntity, getProject());
    }

    private File findMappingFile(Project project) {
        BugsnagPlugin plugin = project.getPlugins().getPlugin(BugsnagPlugin.class);
        AppExtension android = project.getExtensions().getByType(AppExtension.class);
        if (plugin.hasDexguardPlugin(project) && plugin.hasMultipleOutputs(android)) {
            File mappingFile = findDexguardMappingFile(project);

            if (mappingFile != null && mappingFile.exists()) {
                return mappingFile;
            } else {
                project.getLogger().warn("Could not find DexGuard mapping file at: $mappingFile -" +
                    " falling back to AGP mapping file value");
            }
        }

        // Use AGP supplied value, preferring the new "getMappingFileProvider" API but falling back
        // to the old "mappingFile" API if necessary
        try {
            FileCollection mappingFileProvider = variant.getMappingFileProvider().getOrNull();

            // We will warn about not finding a mapping file later, so there's no need to warn here
            if (mappingFileProvider == null || mappingFileProvider.isEmpty()) {
                return null;
            }
            return mappingFileProvider.getSingleFile();
        } catch (Throwable exc) {
            return variant.getMappingFile();
        }
    }

    /**
     * Retrieves the location of a DexGuard mapping file for the given variantOutput. The expected location for this
     * is: build/outputs/mapping/<productFlavor>/<buildType>/<split>
     *
     * variant.mappingFile cannot currently be overridden using the AGP DSL on a per-variantOutput basis, which
     * necessitates this workaround. https://issuetracker.google.com/issues/78921539
     */
    File findDexguardMappingFile(Project project) {
        String buildDir = project.getBuildDir().toString();
        String outputDir = variantOutput.getDirName();

        if (variantOutput.getDirName().endsWith("dpi" + File.separator)) {
            outputDir = new File(variantOutput.getDirName()).getParent();

            if (outputDir == null) { // if only density splits enabled
                outputDir = "";
            }
        }
        return Paths.get(buildDir, "outputs", "mapping", variant.getDirName(), outputDir, "mapping.txt").toFile();
    }

}
