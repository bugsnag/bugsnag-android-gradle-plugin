package com.bugsnag.android.gradle

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

internal object GradleVersions {
    val VERSION_5_3: VersionNumber = VersionNumber.parse("5.3")
    val VERSION_6: VersionNumber = VersionNumber.parse("6.0")
    val VERSION_6_6: VersionNumber = VersionNumber.parse("6.6")
}

internal fun Gradle.versionNumber(): VersionNumber = VersionNumber.parse(gradleVersion)

/* Borrowed helper functions from the Gradle Kotlin DSL. */

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
internal inline fun <reified T : Task> TaskContainer.register(name: String, noinline configuration: T.() -> Unit): TaskProvider<T> =
    register(name, T::class.javaObjectType, configuration)
