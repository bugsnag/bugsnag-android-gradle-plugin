package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariantOutput
import com.bugsnag.android.gradle.BugsnagPlugin.SplitsInfo

class ManifestOutputDir {

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
    static File getManifestPath(BaseVariantOutput output, SplitsInfo splitsInfo) {
        File directory = output.processManifest.manifestOutputDirectory

//        if (split.length == 2) { // only 1 split enabled
//            directory = new File(directory, split[1])
//        } else if (split.length > 2) { // more than 1 split, need to determine order
//            def subdir = split[2] + File.separator + split[1] // N.B. order is reversed!
//            directory = new File(directory, subdir)
//        }
        return new File(directory, "AndroidManifest.xml");
    }

}
