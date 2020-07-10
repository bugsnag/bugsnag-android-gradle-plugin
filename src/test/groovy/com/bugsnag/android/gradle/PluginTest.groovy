package com.bugsnag.android.gradle

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

@CompileStatic
class PluginTest {

    private Project proj

    @Before
    void setUp() throws Exception {
        proj = ProjectBuilder.builder().build()
        proj.pluginManager.apply 'com.bugsnag.android.gradle'
    }

    @Test
    void ensureNoTasksApplied() {
        // no variants, so no tasks applied
        assertEquals(0, proj.tasks.size())
    }

    @Test
    void testVariantName() throws Exception {
        BugsnagPlugin plugin = proj.plugins.getPlugin("com.bugsnag.android.gradle") as BugsnagPlugin
        String name = plugin.taskNameForVariant(new FakeVariantImpl("javaExample"))
        assertEquals("JavaExample", name)
    }

    @Test
    void testOutputName() throws Exception {
        BugsnagPlugin plugin = proj.plugins.getPlugin("com.bugsnag.android.gradle") as BugsnagPlugin
        String name = plugin.taskNameForOutput(new FakeVariantOutputImpl("javaExample-hdpi"))
        assertEquals("JavaExample-hdpi", name)
    }

}
