@file:Suppress("MatchingDeclarationName", "TooManyFunctions") // This file contains multiple top-level members
package com.bugsnag.android.gradle.internal

import com.android.build.VariantOutput
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
// TODO use the new replacement when min AGP version is 4.0
import com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION
import okio.HashingSink
import okio.blackholeSink
import okio.buffer
import okio.source
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.semver.Version
import java.io.File

internal object GradleVersions {
    val VERSION_6_1: Version = Version.parse("6.1")
}

internal object AgpVersions {
    // Use baseVersion to avoid any qualifiers like `-alpha06`
    val CURRENT: Version = Version.parse(ANDROID_GRADLE_PLUGIN_VERSION).toReleaseVersion()
    val VERSION_8_0: Version = Version.parse("8.0.0")
    val VERSION_9_0: Version = Version.parse("9.0.0")
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

@Suppress("SpreadOperator")
internal fun <T : Task> TaskProvider<out T>.dependsOn(vararg tasks: TaskProvider<out Task>): TaskProvider<out T> {
    if (tasks.isEmpty().not()) {
        configure { it.dependsOn(*tasks) }
    }

    return this
}

/** An alternative to [BaseVariant.register] that accepts a [TaskProvider] input. */
internal fun ApkVariant.register(project: Project, provider: TaskProvider<out Task>, autoRunTask: Boolean) {
    if (autoRunTask) {
        assembleProvider.configure { task ->
            task.dependsOn(provider)
        }
        getBundleProvider(project)?.configure { task ->
            task.dependsOn(provider)
        }
    }

    provider.configure { task ->
        task.mustRunAfter(packageApplicationProvider)
    }
    packageApplicationProvider.configure {
        // triggers configuration of the bugsnag upload task so that it runs
        // automatically when an assemble/bundle task is invoked
        if (autoRunTask) {
            provider.get()
        }
    }
}

/**
 * Fetches the [TaskProvider] for the variant's bundle task, or null if it cannot be found
 */
private fun ApkVariant.getBundleProvider(project: Project) = runCatching {
    val bundleName = "bundle" + assembleProvider.name.removePrefix("assemble")
    project.tasks.named(bundleName)
}.getOrNull()

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
 * Returns true if an APK variant output includes SO files for the given ABI.
 */
internal fun ApkVariantOutput.includesAbi(abi: String): Boolean {
    val splitArch = getFilter(VariantOutput.FilterType.ABI)
    return splitArch == null || abi == splitArch
}

/** Returns a String provider for a system property. */
internal fun ProviderFactory.systemPropertyCompat(
    name: String,
    gradleVersion: Version?
): Provider<String> {
    return if (gradleVersion != null && gradleVersion >= GradleVersions.VERSION_6_1) {
        systemProperty(name)
    } else {
        provider { System.getProperty(name) }
    }
}

/**
 * Clears a directory of any files it contains.
 */
internal fun File.clearDir() {
    deleteRecursively()
    mkdir()
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
internal inline fun <reified T : Any> ObjectFactory.newInstance(vararg parameters: Any): T =
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
