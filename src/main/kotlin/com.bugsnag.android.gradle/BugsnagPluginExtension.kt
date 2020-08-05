package com.bugsnag.android.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.util.ConfigureUtil
import java.io.File

// To make kotlin happy with gradle's nullability
private val NULL_STRING: String? = null
private val NULL_BOOLEAN: Boolean? = null

/**
 * Defines configuration options (Gradle plugin extensions) for the BugsnagPlugin
 */
// After Gradle 5.2, this can use service injection for injecting ObjectFactory
open class BugsnagPluginExtension(objects: ObjectFactory) {

    val sourceControl: SourceControl = objects.newInstance(SourceControl::class.java)

    val enabled: Property<Boolean> = objects.property(Boolean::class.javaObjectType)
        .convention(true)

    val uploadJvmMappings: Property<Boolean> = objects.property(Boolean::class.javaObjectType)
        .convention(true)

    val uploadNdkMappings: Property<Boolean> = objects.property(Boolean::class.javaObjectType)
        .convention(NULL_BOOLEAN)

    val reportBuilds: Property<Boolean> = objects.property(Boolean::class.javaObjectType)
        .convention(true)

    val uploadDebugBuildMappings: Property<Boolean> = objects.property(Boolean::class.javaObjectType)
        .convention(false)

    val autoUpdateBuildUuid: Property<Boolean> = objects.property(Boolean::class.javaObjectType)
        .convention(true)

    val endpoint: Property<String> = objects.property(String::class.javaObjectType)
        .convention("https://upload.bugsnag.com")

    val releasesEndpoint = objects.property(String::class.javaObjectType)
        .convention("https://build.bugsnag.com")

    val overwrite: Property<Boolean> = objects.property(Boolean::class.javaObjectType)
        .convention(false)

    val retryCount: Property<Int> = objects.property(Int::class.javaObjectType)
        .convention(0)

    val sharedObjectPaths: ListProperty<File> = objects.listProperty(File::class.java)
        .convention(emptyList())

    val projectRoot: Property<String> = objects.property(String::class.java).convention(NULL_STRING)

    val failOnUploadError: Property<Boolean> = objects.property(Boolean::class.javaObjectType)
        .convention(true)

    val requestTimeoutMs: Property<Long> = objects.property(Long::class.javaObjectType)
        .convention(60000)

    // release API values
    val builderName: Property<String> = objects.property(String::class.java).convention(NULL_STRING)

    val metadata: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)
        .convention(emptyMap())

    val objdumpPaths: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)
        .convention(emptyMap())

    // exposes sourceControl as a nested object on the extension,
    // see https://docs.gradle.org/current/userguide/custom_gradle_types.html#nested_objects

    fun sourceControl(closure: Closure<SourceControl>) {
        ConfigureUtil.configure(closure, sourceControl)
    }

    fun sourceControl(action: Action<SourceControl>) {
        action.execute(sourceControl)
    }
}
