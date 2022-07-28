package com.bugsnag.android.gradle.internal

import com.android.build.api.variant.VariantOutput
import com.android.build.api.variant.impl.VariantOutputImpl
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

internal interface VariantTaskCompanion<T : Task> {
    fun taskNameFor(variantOutputName: String): String
    fun taskNameFor(output: BaseVariantOutput): String = taskNameFor(output.name)
    fun taskNameFor(output: VariantOutput): String =
        taskNameFor((output as VariantOutputImpl).baseName)
}

internal inline fun <reified T : Task> VariantTaskCompanion<T>.register(
    project: Project,
    output: BaseVariantOutput,
    noinline configure: T.() -> Unit
) = project.tasks.register(taskNameFor(output), configure)

internal inline fun <reified T : Task> VariantTaskCompanion<T>.register(
    project: Project,
    output: VariantOutput,
    noinline configure: T.() -> Unit
) = project.tasks.register(taskNameFor(output), configure)

internal inline fun <reified T : Task> VariantTaskCompanion<T>.forBuildOutput(
    project: Project,
    output: BaseVariantOutput
): TaskProvider<T> {
    return project.tasks
        .withType(T::class.java)
        .named(taskNameFor(output))
}
