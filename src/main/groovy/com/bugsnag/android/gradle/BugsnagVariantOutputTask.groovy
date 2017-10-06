package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.DefaultTask

class BugsnagVariantOutputTask extends DefaultTask {

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
    def File getManifestPath() {
        File directory = variantOutput.processManifest.manifestOutputDirectory
        String[] tokens = variantOutput.name.split("-")

        // when splits are enabled, the output has a name with the following structure:
        // e.g. "javaExample-hdpiMips-debug"

        if (tokens.length == 3) {
            def split = tokens[1]
            BugsnagPlugin.SplitsInfo splitsInfo = null // TODO populate!

            if (splitsInfo != null) {
                def density = findValueForSplit(split, splitsInfo.densityFilters)
                def abi = findValueForSplit(split, splitsInfo.densityFilters)
                directory = findManifestDirForSplit(density, abi, directory)
            }
        }
        new File(directory, "AndroidManifest.xml")
    }

    private static String findValueForSplit(String split, Collection<String> values) {
        for (String val : values) {
            if (split.contains(val)) {
                return split
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
