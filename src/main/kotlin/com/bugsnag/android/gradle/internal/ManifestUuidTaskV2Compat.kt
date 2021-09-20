package com.bugsnag.android.gradle.internal

import com.android.build.api.artifact.Artifacts
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.bugsnag.android.gradle.BugsnagManifestUuidTaskV2
import com.bugsnag.android.gradle.BugsnagPluginExtension
import com.bugsnag.android.gradle.VariantFilterImpl
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.util.UUID

/**
 * Registers a [BugsnagManifestUuidTaskV2] for the given variant.
 */
internal fun registerV2ManifestUuidTask(
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
        .toTransform(SingleArtifact.MERGED_MANIFEST)
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
