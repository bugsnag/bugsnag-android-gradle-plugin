package com.bugsnag.android.gradle

import com.android.build.FilterData
import com.android.build.VariantOutput
import org.junit.Test

import static com.android.build.VariantOutput.FilterType.*
import static com.bugsnag.android.gradle.BugsnagVariantOutputTask.findApkSplitDir
import static com.bugsnag.android.gradle.BugsnagVariantOutputTask.findManifestDirForSplit
import static org.junit.Assert.assertEquals

class ManifestPathTest {

    @Test
    void testSplitManifestDir() {
        File dir = new File("build")

        def abiAndDensity = new File(new File(dir, "armeabi-v7a"), "mdpi")
        assertEquals(abiAndDensity, findManifestDirForSplit("mdpi", "armeabi-v7a", dir))

        def abiOnly = new File(dir, "x86")
        assertEquals(abiOnly, findManifestDirForSplit(null, "x86", dir))

        def densityOnly = new File(dir, "xhdpi")
        assertEquals(densityOnly, findManifestDirForSplit("xhdpi", null, dir))

        // default case, no apk splits
        assertEquals(dir, findManifestDirForSplit(null, null, dir))
    }

    @Test
    void testFindApkSplitDir() {
        File dir = new File("build")

        def abiAndDensityFilters = Arrays.asList(
            new FakeFilterData("x86_64", ABI.toString()),
            new FakeFilterData("hdpi", DENSITY.toString())
        )
        def abiAndDensity = new File(new File(dir, "x86_64"), "hdpi")
        assertEquals(abiAndDensity, findApkSplitDir(abiAndDensityFilters, dir))

        def abiFilters = Collections.singletonList(new FakeFilterData("x86_64", ABI.toString()))
        def abiOnly = new File(dir, "x86_64")
        assertEquals(abiOnly, findApkSplitDir(abiFilters, dir))

        def densityFilters = Collections.singletonList(new FakeFilterData("xxxhdpi", DENSITY.toString()))
        def densityOnly = new File(dir, "xxxhdpi")
        assertEquals(densityOnly, findApkSplitDir(densityFilters, dir))

        // default case, no apk splits
        assertEquals(dir, findApkSplitDir(Collections.emptyList(), dir))
    }

    static class FakeFilterData implements FilterData {
        private final String id
        private final String filterType

        FakeFilterData(String id, String filterType) {
            this.id = id
            this.filterType = filterType
        }

        @Override
        String getIdentifier() {
            return id
        }

        @Override
        String getFilterType() {
            return filterType
        }
    }

}
