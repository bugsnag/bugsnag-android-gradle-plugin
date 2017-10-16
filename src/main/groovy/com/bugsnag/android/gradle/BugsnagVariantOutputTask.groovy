package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.DefaultTask

class BugsnagVariantOutputTask extends DefaultTask {

    private static final String SPLIT_UNIVERSAL = "universal"

    BaseVariantOutput variantOutput

    /**
     * Gets the manifest for a given Variant Output, accounting for any APK splits.
     *
     * Currently supported split types include Density, and ABI. There is also a Language split,
     * but it appears to be broken (see issuetracker)
     *
     * @param output the variant output
     * @return the manifest path
     *
     * See: https://developer.android.com/studio/build/configure-apk-splits.html#build-apks-filename
     * https://issuetracker.google.com/issues/37085185
     */
    File getManifestPath() {
        File directory = variantOutput.processManifest.manifestOutputDirectory
        String[] tokens = variantOutput.name.split("-")

        // when splits are enabled, the output has a name with the following structure:
        // e.g. "javaExample-hdpiMips-debug"

        if (tokens.length == 4) { // should be one of the following cases: 'armeabi-v7a', 'arm64-v8a'
            def split = tokens[1] + "-" + tokens[2] // combine back into one string
            directory = guessManifestDir(directory, split)
        } else if (tokens.length == 3) {
            directory = guessManifestDir(directory, tokens[1])
        } else if (tokens.length > 4) {
            project.logger.warn("Cannot parse `variantOutput.name: ${variantOutput.name}` for manifest location")
        }
        def file = new File(directory, "AndroidManifest.xml")

        if (!file.exists()) {
            project.logger.error("Failed to find manifest at ${file}")
        }
        file
    }

    private File guessManifestDir(File directory, String split) {
        if (SPLIT_UNIVERSAL == split) {
            directory = new File(directory, SPLIT_UNIVERSAL)
        } else {
            def density = findValueForDensityFilter(split, project.ext.splitsInfo.densityFilters)
            def abi = findValueForAbiFilter(split, project.ext.splitsInfo.abiFilters)
            directory = findManifestDirForSplit(density, abi, directory)
        }
        directory
    }

    private static String findValueForAbiFilter(String split, Collection<String> values) {
        for (String val : values) {
            if (split.toLowerCase().endsWith(val.toLowerCase())) {
                return val
            }
        }
        null
    }

    private static String findValueForDensityFilter(String split, Collection<String> values) {
        for (String val : values) {
            if (split.toLowerCase().startsWith(val.toLowerCase())) {
                return val
            }
        }
        null
    }

    private static File findManifestDirForSplit(String density, String abi, File manifestDir) {
        if (abi != null && density != null) { // abi comes first, then density
            String subdir = abi + File.separator + density
            new File(manifestDir, subdir)
        } else if (abi != null) {
            new File(manifestDir, abi)
        } else if (density != null) {
            new File(manifestDir, density)
        } else {
            manifestDir
        }
    }

}
