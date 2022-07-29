package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.listProperty
import com.bugsnag.android.gradle.internal.mapProperty
import com.bugsnag.android.gradle.internal.newInstance
import com.bugsnag.android.gradle.internal.property
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.util.ConfigureUtil
import java.io.File
import java.util.Locale
import javax.inject.Inject

// To make kotlin happy with gradle's nullability
private val NULL_STRING: String? = null
private val NULL_BOOLEAN: Boolean? = null
private val NULL_FILE: File? = null

internal const val UPLOAD_ENDPOINT_DEFAULT: String = "https://upload.bugsnag.com"

/**
 * Defines configuration options (Gradle plugin extensions) for the BugsnagPlugin
 */
open class BugsnagPluginExtension @Inject constructor(objects: ObjectFactory) {

    val sourceControl: SourceControl = objects.newInstance()

    val enabled: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    val uploadJvmMappings: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    val uploadNdkMappings: Property<Boolean> = objects.property<Boolean>()
        .convention(NULL_BOOLEAN)

    val uploadNdkUnityLibraryMappings: Property<Boolean> = objects.property<Boolean>()
        .convention(NULL_BOOLEAN)

    val uploadReactNativeMappings: Property<Boolean> = objects.property<Boolean>()
        .convention(NULL_BOOLEAN)

    val reportBuilds: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    val uploadDebugBuildMappings: Property<Boolean> = objects.property<Boolean>()
        .convention(false)

    val endpoint: Property<String> = objects.property<String>()
        .convention(UPLOAD_ENDPOINT_DEFAULT)

    val releasesEndpoint = objects.property<String>()
        .convention("https://build.bugsnag.com")

    val overwrite: Property<Boolean> = objects.property<Boolean>()
        .convention(false)

    val retryCount: Property<Int> = objects.property<Int>()
        .convention(0)

    val sharedObjectPaths: ListProperty<File> = objects.listProperty<File>()
        .convention(emptyList())

    val nodeModulesDir: Property<File> = objects.property<File>()
        .convention(NULL_FILE)

    val projectRoot: Property<String> = objects.property<String>().convention(NULL_STRING)

    val failOnUploadError: Property<Boolean> = objects.property<Boolean>()
        .convention(true)

    val requestTimeoutMs: Property<Long> = objects.property<Long>()
        .convention(60000)

    // release API values
    val builderName: Property<String> = objects.property<String>().convention(NULL_STRING)

    val metadata: MapProperty<String, String> = objects.mapProperty<String, String>()
        .convention(emptyMap())

    val objdumpPaths: MapProperty<String, String> = objects.mapProperty<String, String>()
        .convention(emptyMap())

    // exposes sourceControl as a nested object on the extension,
    // see https://docs.gradle.org/current/userguide/custom_gradle_types.html#nested_objects

    fun sourceControl(closure: Closure<SourceControl>) {
        ConfigureUtil.configure(closure, sourceControl)
    }

    fun sourceControl(action: Action<SourceControl>) {
        action.execute(sourceControl)
    }

    internal var filter: Action<VariantFilter> = Action {
        if (it.name.toLowerCase(Locale.ENGLISH).contains("debug")) {
            it.setEnabled(false)
        }
    }

    fun variantFilter(action: Action<VariantFilter>) {
        this.filter = action
    }
}

interface VariantFilter {
    val name: String
    fun setEnabled(enabled: Boolean)
}

internal class VariantFilterImpl(override val name: String) : VariantFilter {
    internal var variantEnabled: Boolean? = true

    override fun setEnabled(enabled: Boolean) {
        this.variantEnabled = enabled
    }
}
