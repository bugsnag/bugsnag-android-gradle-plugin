package com.bugsnag.android.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.util.ConfigureUtil
import java.io.File
import javax.inject.Inject

// To make kotlin happy with gradle's nullability
private val NULL_STRING: String? = null

/**
 * Defines configuration options (Gradle plugin extensions) for the BugsnagPlugin
 */
open class BugsnagPluginExtension @Inject constructor(objects: ObjectFactory) {

    val sourceControl: SourceControl = objects.newInstance(SourceControl::class.java)

    var isEnabled = true
    var isUploadJvmMappings = true
    var isUploadNdkMappings: Boolean? = null
    var isReportBuilds = true
    var isUploadDebugBuildMappings = false
    val endpoint: Property<String> = objects.property(String::class.javaObjectType)
        .convention("https://upload.bugsnag.com")
    var releasesEndpoint = "https://build.bugsnag.com"
    val overwrite: Property<Boolean> = objects.property(Boolean::class.javaObjectType)
        .convention(false)
    val retryCount: Property<Int> = objects.property(Int::class.javaObjectType)
        .convention(0)
    var sharedObjectPaths: List<File> = emptyList()
    var projectRoot: String? = null
    val failOnUploadError: Property<Boolean> = objects.property(Boolean::class.javaObjectType)
        .convention(true)
    val requestTimeoutMs: Property<Long> = objects.property(Long::class.javaObjectType)
        .convention(60000)

    // release API values
    var builderName: Property<String> = objects.property(String::class.java).convention(NULL_STRING)
    var metadata: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)
        .convention(emptyMap())
    var objdumpPaths: Map<String, String>? = null

    // exposes sourceControl as a nested object on the extension,
    // see https://docs.gradle.org/current/userguide/custom_gradle_types.html#nested_objects

    fun sourceControl(closure: Closure<SourceControl>) {
        ConfigureUtil.configure(closure, sourceControl)
    }

    fun sourceControl(action: Action<SourceControl>) {
        action.execute(sourceControl)
    }
}
