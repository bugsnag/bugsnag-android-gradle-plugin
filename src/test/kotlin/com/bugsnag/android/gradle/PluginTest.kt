package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
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
    lateinit var variant: ApkVariant

    @Mock
    lateinit var variantOutput: ApkVariantOutput

    @Test
    fun ensureNoTasksApplied() {
        // no variants, so no tasks applied
        assertEquals(0, proj.tasks.size.toLong())
    }

    @Test
    fun testVariantName() {
        val plugin = proj.plugins.getPlugin(BugsnagPlugin::class.java)
        `when`(variant.name).thenReturn("javaExample")
        assertEquals("JavaExample", plugin.taskNameForVariant(variant))
    }

    @Test
    fun testOutputName() {
        val plugin = proj.plugins.getPlugin(BugsnagPlugin::class.java)
        `when`(variantOutput.name).thenReturn("javaExample-hdpi")
        assertEquals("JavaExample-hdpi", plugin.taskNameForOutput(variantOutput))
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
