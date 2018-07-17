package com.bugsnag.android.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals

class ManifestPathTest {

    private BugsnagVariantOutputTask task

    @Before
    void setUp() throws Exception {
        Project proj = ProjectBuilder.builder().build()
        proj.pluginManager.apply 'com.bugsnag.android.gradle'
        task = proj.getTasks().create("variantTask", BugsnagVariantOutputTask.class)
    }

    @Test
    void testSplitManifestDir() {
        File dir = new File("build")

        def abiAndDensity = new File(new File(dir, "armeabi-v7a"), "mdpi")
        assertEquals(abiAndDensity, task.findManifestDirForSplit("mdpi", "armeabi-v7a", dir))

        def abiOnly = new File(dir, "x86")
        assertEquals(abiOnly, task.findManifestDirForSplit(null, "x86", dir))

        def densityOnly = new File(dir, "xhdpi")
        assertEquals(densityOnly, task.findManifestDirForSplit("xhdpi", null, dir))

        // default case, no apk splits
        assertEquals(dir, task.findManifestDirForSplit(null, null, dir))
    }

}
