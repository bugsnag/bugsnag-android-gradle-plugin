package com.bugsnag.android.gradle;

import org.gradle.api.tasks.TaskAction;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Task to add a unique build UUID to AndroidManifest.xml during the build
 * process. This is used by Bugsnag to identify which proguard mapping file
 * should be used to deobfuscate each crash report.
 * <p>
 * https://docs.gradle.org/current/userguide/custom_tasks.html
 * <p>
 * This task must be called after "process${variantName}Manifest", since it
 * requires that an AndroidManifest.xml exists in `build/intermediates`.
 */
public class BugsnagManifestTask extends BugsnagVariantOutputTask {

    @TaskAction
    void updateManifest() throws ParserConfigurationException, SAXException, IOException {
        List<File> paths = getManifestPaths();

        // Uniquely identify the build so that we can identify the proguard file.
        String buildUUID = UUID.randomUUID().toString();

        for (File manifestPath : paths) {
            if (!manifestPath.exists()) {
                continue;
            }
            getProject().getLogger().debug("Updating manifest with build UUID: " + manifestPath);
            new AndroidManifestParser().writeBuildUuid(manifestPath, buildUUID, getLogger());

        }
    }
}
