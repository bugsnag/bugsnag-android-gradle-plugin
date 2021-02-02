package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariant
import org.gradle.api.Project
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class BugsnagUploadJsSourceMapTest {

    @Mock
    lateinit var variant: ApkVariant

    @Mock
    lateinit var project: Project

    @Before
    fun setUp() {
        `when`(project.buildDir).thenReturn(File("/build"))
        `when`(variant.dirName).thenReturn("release")
    }

    @Test
    fun testFindReactNativeSourcemapFile() {
        assertEquals(
            "/build/generated/sourcemaps/react/release/index.android.bundle.map",
            findReactNativeSourcemapFile(project, variant)
        )
    }

    @Test
    fun testCustomBundleName() {
        `when`(project.property("react")).thenReturn(mapOf(Pair("bundleAssetName", "foo")))
        assertEquals(
            "/build/generated/sourcemaps/react/release/foo.map",
            findReactNativeSourcemapFile(project, variant)
        )
    }
}
