@file:Suppress("MatchingDeclarationName") // This file contains multiple top-level members
package com.bugsnag.android.gradle.internal

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
// TODO use the new replacement when min AGP version is 4.0
import com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.scope.TaskContainer as AgpTaskContainer
import com.android.build.gradle.internal.scope.MutableTaskContainer
import okio.HashingSink
import okio.blackholeSink
import okio.buffer
import okio.source
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.util.VersionNumber
import java.io.File

internal object GradleVersions {
    val VERSION_5_3: VersionNumber = VersionNumber.parse("5.3")
    val VERSION_6: VersionNumber = VersionNumber.parse("6.0")
    val VERSION_6_1: VersionNumber = VersionNumber.parse("6.1")
    val VERSION_6_6: VersionNumber = VersionNumber.parse("6.6")
}

internal fun Gradle.versionNumber(): VersionNumber = VersionNumber.parse(gradleVersion)

internal object AgpVersions {
    // Use baseVersion to avoid any qualifiers like `-alpha06`
    val CURRENT: VersionNumber = VersionNumber.parse(ANDROID_GRADLE_PLUGIN_VERSION).baseVersion
    val VERSION_3_4: VersionNumber = VersionNumber.parse("3.4.0")
    val VERSION_4_0: VersionNumber = VersionNumber.parse("4.0.0")
}

/** A fast file hash that don't load the entire file contents into memory at once. */
internal fun File.md5HashCode(): Int {
    return HashingSink.md5(blackholeSink()).use { sink ->
        source().buffer().use { fileSource ->
            fileSource.readAll(sink)
        }
        sink.hash.hashCode()
    }
}

internal fun <T: Task> TaskProvider<out T>.dependsOn(vararg tasks: TaskProvider<out Task>): TaskProvider<out T> {
    if (tasks.isEmpty().not()) {
        configure { it.dependsOn(*tasks) }
    }

    return this
}

/** An alternative to [BaseVariant.register] that accepts a [TaskProvider] input. */
internal fun BaseVariant.register(project: Project, provider: TaskProvider<out Task>) {
    val success = when {
        AgpVersions.CURRENT >= AgpVersions.VERSION_4_0 -> {
            registerAgp4(provider)
        }
        AgpVersions.CURRENT >= AgpVersions.VERSION_3_4 -> {
            registerAgp3(provider)
        }
        else -> false
    }

    if (!success) {
        registerManual(project, provider)
    }
}

private fun BaseVariant.registerAgp4(provider: TaskProvider<out Task>): Boolean {
    return try {
        // This is of type ComponentPropertiesImpl
        val componentProperties = javaClass.getField("componentProperties")
            .apply { isAccessible = true }
            .get(this)
        val taskContainer = componentProperties.javaClass.getMethod("getTaskContainer")
            .apply { isAccessible = true }
            .invoke(componentProperties) as AgpTaskContainer
        taskContainer.register(provider)
        true
    } catch (t: Throwable) {
        false
    }
}

private fun BaseVariant.registerAgp3(provider: TaskProvider<out Task>): Boolean {
    return try {
        if (this is ApplicationVariantImpl) {
            variantData.taskContainer.register(provider)
        }
        true
    } catch (t: Throwable) {
        false
    }
}

private fun AgpTaskContainer.register(provider: TaskProvider<out Task>) {
    assembleTask.dependsOn(provider)
    bundleLibraryTask?.dependsOn(provider)
    if (this is MutableTaskContainer) {
        bundleTask?.dependsOn(provider)
    }
}

private fun BaseVariant.registerManual(project: Project, provider: TaskProvider<out Task>) {
    assembleProvider.dependsOn(provider)
    val bundleName = "bundle" + assembleProvider.name.removePrefix("assemble")
    project.tasks.matching { it.name == bundleName }
        .configureEach {
            it.dependsOn(provider)
        }
}

/**
 * Returns true if a project has configured multiple variant outputs.
 *
 * This calculation is based on a heuristic - the number of variantOutputs in a project must be
 * greater than the number of variants.
 */
internal fun AppExtension.hasMultipleOutputs(): Boolean {
    val variants: DomainObjectSet<ApplicationVariant> = applicationVariants
    val variantSize = variants.count()
    var outputSize = 0

    variants.forEach { variant ->
        outputSize += variant.outputs.count()
    }
    return outputSize > variantSize
}

/**
 * Returns true if the DexGuard plugin has been applied to the project
 */
fun Project.hasDexguardPlugin(): Boolean {
    return pluginManager.hasPlugin("dexguard")
}

/* Borrowed helper functions from the Gradle Kotlin DSL. */

/**
 * Create a new instance of [T], using [parameters] as the construction parameters.
 *
 * @param T The type of object to create
 * @param parameters The construction parameters
 * @return the created named object
 *
 * @see [ObjectFactory.newInstance]
 */
@Suppress("SpreadOperator")
inline fun <reified T : Any> ObjectFactory.newInstance(vararg parameters: Any): T =
    newInstance(T::class.javaObjectType, *parameters)

/**
 * Creates a [Property] that holds values of the given type [T].
 *
 * @see [ObjectFactory.property]
 */
internal inline fun <reified T : Any> ObjectFactory.property(): Property<T> =
    property(T::class.javaObjectType)

/**
 * Creates a [SetProperty] that holds values of the given type [T].
 *
 * @see [ObjectFactory.setProperty]
 */
internal inline fun <reified T : Any> ObjectFactory.setProperty(): SetProperty<T> =
    setProperty(T::class.javaObjectType)

/**
 * Creates a [ListProperty] that holds values of the given type [T].
 *
 * @see [ObjectFactory.listProperty]
 */
internal inline fun <reified T : Any> ObjectFactory.listProperty(): ListProperty<T> =
    listProperty(T::class.javaObjectType)

/**
 * Creates a [MapProperty] that holds values of the given key type [K] and value type [V].
 *
 * @see [ObjectFactory.mapProperty]
 */
internal inline fun <reified K : Any, reified V : Any> ObjectFactory.mapProperty(): MapProperty<K, V> =
    mapProperty(K::class.javaObjectType, V::class.javaObjectType)

/**
 * Defines and configure a new task, which will be created when it is required.
 *
 * @see [TaskContainer.register]
 */
internal inline fun <reified T : Task> TaskContainer.register(
    name: String,
    noinline configuration: T.() -> Unit
): TaskProvider<T> = register(name, T::class.javaObjectType, configuration)
