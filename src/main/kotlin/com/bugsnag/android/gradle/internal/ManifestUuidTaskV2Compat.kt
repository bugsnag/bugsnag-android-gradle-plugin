package com.bugsnag.android.gradle.internal

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.artifact.Artifacts
import com.android.build.api.extension.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.bugsnag.android.gradle.BugsnagManifestUuidTaskV2
import com.bugsnag.android.gradle.BugsnagPluginExtension
import com.bugsnag.android.gradle.GroovyCompat
import com.bugsnag.android.gradle.VariantFilterImpl
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.util.UUID

/**
 * Registers a [BugsnagManifestUuidTaskV2] for the given variant.
 */
internal fun registerV2ManifestUuidTask(
    android: AppExtension,
    bugsnag: BugsnagPluginExtension,
    project: Project
) {
    when {
        AgpVersions.CURRENT >= AgpVersions.VERSION_4_2 -> registerUuidTaskAGP42(bugsnag, project)
        AgpVersions.CURRENT >= AgpVersions.VERSION_4_1 -> registerUuidTaskAGP41(android, bugsnag, project)
        else -> {
        }
    }
}

/**
 * Registers a [BugsnagManifestUuidTaskV2] for a project using AGP 4.2+. This
 * version of AGP uses `androidComponents.onVariants` to allow task registration
 * for variants.
 */
@Suppress("UnstableApiUsage")
private fun registerUuidTaskAGP42(
    bugsnag: BugsnagPluginExtension,
    project: Project
) {
    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
    androidComponents.onVariants { variant ->
        val manifestUpdater = createManifestUpdateTask(bugsnag, project, variant.name)
        manifestUpdater?.let {
            wireManifestUpdaterTask(manifestUpdater, variant.artifacts)
        }
    }
}

/**
 * Registers a [BugsnagManifestUuidTaskV2] for a project using AGP 4.1. This
 * version of AGP uses `android.onVariants.onProperties` to allow task registration
 * for variants.
 */
@Suppress("UnstableApiUsage")
private fun registerUuidTaskAGP41(
    android: AppExtension,
    bugsnag: BugsnagPluginExtension,
    project: Project
) {
    var manifestUpdater: TaskProvider<BugsnagManifestUuidTaskV2>? = null
    val onVariants = Action<String> { name ->
        manifestUpdater = createManifestUpdateTask(bugsnag, project, name)
    }
    val onProperties = Action<Artifacts> { artifacts ->
        manifestUpdater?.apply {
            wireManifestUpdaterTask(this, artifacts)
        }
    }
    GroovyCompat.registerUuidTaskAGP41(android, onVariants, onProperties)
}

internal fun createManifestUpdateTask(
    bugsnag: BugsnagPluginExtension,
    project: Project,
    variantName: String
): TaskProvider<BugsnagManifestUuidTaskV2>? {
    if (!bugsnag.enabled.get()) {
        return null
    }
    val variantFilterImpl = VariantFilterImpl(variantName)
    if (!isVariantEnabled(bugsnag, variantFilterImpl)) {
        return null
    }
    val taskName = taskNameForManifestUuid(variantName)
    val manifestInfoOutputFile = project.computeManifestInfoOutputV2(variantName)
    val buildUuidProvider = project.newUuidProvider()
    return project.tasks.register(
        taskName,
        BugsnagManifestUuidTaskV2::class.java
    ) {
        it.buildUuid.set(buildUuidProvider)
        it.manifestInfoProvider.set(manifestInfoOutputFile)
    }
}

/**
 * Wires the manifest UUID task up so that it transforms each variant's AndroidManifest.
 */
internal fun wireManifestUpdaterTask(
    manifestUpdater: TaskProvider<BugsnagManifestUuidTaskV2>,
    artifacts: Artifacts
) {
    artifacts
        .use(manifestUpdater)
        .wiredWithFiles(
            taskInput = BugsnagManifestUuidTaskV2::inputManifest,
            taskOutput = BugsnagManifestUuidTaskV2::outputManifest
        )
        .toTransform(ArtifactType.MERGED_MANIFEST)
}

internal fun isVariantEnabled(
    bugsnag: BugsnagPluginExtension,
    variant: VariantFilterImpl
): Boolean {
    bugsnag.filter.execute(variant)
    return variant.variantEnabled ?: true
}

internal fun Project.newUuidProvider(): Provider<String> {
    return provider {
        UUID.randomUUID().toString()
    }
}
