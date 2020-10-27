package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariantOutput
import com.bugsnag.android.gradle.internal.taskNameSuffix
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PluginTest {

    @Mock
    lateinit var variantOutput: ApkVariantOutput

    @Test
    fun ensureNoTasksApplied() {
        // no variants, so no tasks applied
        assertEquals(0, proj.tasks.size.toLong())
    }

    @Test
    fun testOutputName() {
        `when`(variantOutput.name).thenReturn("javaExample-hdpi")
        assertEquals("JavaExample-hdpi", variantOutput.taskNameSuffix())
    }

    companion object {
        private lateinit var proj: Project

        @BeforeClass
        @JvmStatic
        fun setUp() {
            proj = ProjectBuilder.builder().build()
            proj.pluginManager.apply("com.bugsnag.android.gradle")
        }
    }
}
