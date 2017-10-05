package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariantOutput

class ManifestOutputDir {

    static File getManifestPath(BaseVariantOutput output) {
        File directory = output.getProcessManifest().getManifestOutputDirectory();
        String name = output.getName();
        String[] split = name.split("-");

        // need to account for APK splits, see:
        // https://developer.android.com/studio/build/configure-apk-splits.html#build-apks-filename

        if (split.length > 1) {
            directory = new File(output.getProcessManifest().getManifestOutputDirectory(), split[1]);
        }
        return new File(directory, "AndroidManifest.xml");
    }

}
