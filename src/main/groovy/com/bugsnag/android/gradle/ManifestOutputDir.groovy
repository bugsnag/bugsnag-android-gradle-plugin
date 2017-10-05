package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariantOutput

class ManifestOutputDir {

    /**
     * Gets the manifest for a given Variant Output, accounting for any APK splits.
     *
     * Currently supported split types include Density, ABI, and Language.
     *
     * @param output the variant output
     * @return the manifest path
     *
     * See: https://developer.android.com/studio/build/configure-apk-splits.html#build-apks-filename
     */
    static File getManifestPath(BaseVariantOutput output) {
        File directory = output.processManifest.manifestOutputDirectory
        String[] split = output.name.split("-")

        if (split.length == 2) { // only 1 split enabled
            directory = new File(directory, split[1])
        } else if (split.length > 2) { // more than 1 split, need to determine order
            def subdir = split[3] + File.separator + split[2] // N.B. order is reversed!
            directory = new File(directory, subdir)
        }
        return new File(directory, "AndroidManifest.xml");
    }

}
