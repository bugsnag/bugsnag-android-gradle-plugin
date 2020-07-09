package com.bugsnag.android.gradle;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.tasks.ManifestProcessorTask;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Provider;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BugsnagVariantOutputUtils {

    /**
     * Gets the manifest for a given Variant Output, accounting for any APK splits.
     *
     * Currently supported split types include Density, and ABI. There is also a Language split,
     * but it appears to be broken (see issuetracker)
     *
     * @param project the current project
     * @param variant the variant
     * @param variantOutput the variantOutput
     * @return the manifest path
     *
     * See: https://developer.android.com/studio/build/configure-apk-splits.html#build-apks-filename
     * https://issuetracker.google.com/issues/37085185
     */
    static List<File> getManifestPaths(Project project, BaseVariant variant, BaseVariantOutput variantOutput) {
        File directoryMerged = null;
        File directoryBundle;
        List<File> manifestPaths = new ArrayList();

        boolean getMergedManifest = BugsnagPlugin.isRunningAssembleTask(variant, variantOutput, project);
        boolean getBundleManifest = BugsnagPlugin.isRunningBundleTask(variant, variantOutput, project);

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
            directoryMerged = AgpCompat.getManifestOutputDir(processManifest);

            if (directoryMerged != null) {
                addManifestPath(manifestPaths, directoryMerged, project.getLogger(), variantOutput);
            }
        }

        // Attempt to get the bundle manifest directory if required
        if (getBundleManifest) {
            directoryBundle = BugsnagPlugin.resolveBundleManifestOutputDirectory(processManifest);

            if (directoryBundle != null) {
                addManifestPath(manifestPaths, directoryBundle, project.getLogger(), variantOutput);
            }
        }

        return manifestPaths;
    }

    static void addManifestPath(List<File> manifestPaths, File directory, Logger logger, BaseVariantOutput variantOutput) {
        File manifestFile = Paths.get(directory.toString(), variantOutput.getDirName(),
            "AndroidManifest.xml").toFile();

        if (manifestFile.exists()) {
            logger.info("Found manifest at ${manifestFile}");
            manifestPaths.add(manifestFile);
        } else {
            logger.error("Failed to find manifest at ${manifestFile}");
        }
    }

    // Read the API key and Build ID etc..
    static AndroidManifestInfo readManifestFile(Project project, BaseVariant variant, BaseVariantOutput variantOutput) throws ParserConfigurationException, SAXException, IOException {
        // Parse the AndroidManifest.xml
        List<File> paths = getManifestPaths(project, variant, variantOutput);

        for (File manifestPath : paths) {
            if (manifestPath.exists()) {
                return new AndroidManifestParser().readManifest(manifestPath, project.getLogger());
            }
        }
        return null;
    }
}
