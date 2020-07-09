package com.bugsnag.android.gradle;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.tasks.ManifestProcessorTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BugsnagVariantOutputTask extends DefaultTask {

    BaseVariantOutput variantOutput;
    BaseVariant variant;
    AndroidManifestInfo manifestInfo;

    /**
     * Gets the manifest for a given Variant Output, accounting for any APK splits.
     *
     * Currently supported split types include Density, and ABI. There is also a Language split,
     * but it appears to be broken (see issuetracker)
     *
     * @return the manifest path
     *
     * See: https://developer.android.com/studio/build/configure-apk-splits.html#build-apks-filename
     * https://issuetracker.google.com/issues/37085185
     */
    List<File> getManifestPaths() {
        File directoryMerged = null;
        File directoryBundle;
        List<File> manifestPaths = new ArrayList();

        boolean getMergedManifest = BugsnagPlugin.isRunningAssembleTask(variant, variantOutput, getProject());
        boolean getBundleManifest = BugsnagPlugin.isRunningBundleTask(variant, variantOutput, getProject());

        // If the manifest location could not be reliably determined, attempt to get both
        if (!getMergedManifest && !getBundleManifest) {
            getMergedManifest = true;
            getBundleManifest = true;
        }

        ManifestProcessorTask processManifest = variantOutput.getProcessManifestProvider().getOrNull();

        if (processManifest == null) {
            return manifestPaths;
        }

        if (getMergedManifest) {
            Provider<Directory> outputDir = processManifest.getManifestOutputDirectory();
            Directory dir = outputDir.getOrNull();

            if (dir != null) {
                directoryMerged = dir.getAsFile();
            }

            if (directoryMerged != null) {
                addManifestPath(manifestPaths, directoryMerged);
            }
        }

        // Attempt to get the bundle manifest directory if required
        if (getBundleManifest) {
            directoryBundle = BugsnagPlugin.resolveBundleManifestOutputDirectory(processManifest);

            if (directoryBundle != null) {
                addManifestPath(manifestPaths, directoryBundle);
            }
        }

        return manifestPaths;
    }

    void addManifestPath(List<File> manifestPaths, File directory) {
        File manifestFile = Paths.get(directory.toString(), variantOutput.getDirName(),
            "AndroidManifest.xml").toFile();

        if (manifestFile.exists()) {
            getProject().getLogger().info("Found manifest at ${manifestFile}");
            manifestPaths.add(manifestFile);
        } else {
            getProject().getLogger().error("Failed to find manifest at ${manifestFile}");
        }
    }

    // Read the API key and Build ID etc..
    void readManifestFile() throws ParserConfigurationException, SAXException, IOException {
        // Parse the AndroidManifest.xml
        List<File> paths = getManifestPaths();

        for (File manifestPath : paths) {
            if (!manifestPath.exists()) {
                continue;
            }
            manifestInfo = new AndroidManifestParser().readManifest(manifestPath, getLogger());
        }
    }
}
