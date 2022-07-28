package com.bugsnag.android.gradle.internal

import com.android.build.api.artifact.Artifacts
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.VariantOutput
import com.android.build.api.variant.impl.VariantOutputImpl
import com.bugsnag.android.gradle.BugsnagManifestUuidTask
import com.bugsnag.android.gradle.BugsnagPluginExtension
import com.bugsnag.android.gradle.VariantFilterImpl
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.util.UUID

/**
 * Registers a [BugsnagManifestUuidTask] for the given variant.
 */
internal fun registerV2ManifestUuidTask(
    bugsnag: BugsnagPluginExtension,
    project: Project
) {
    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
    androidComponents.onVariants { variant ->
        (variant as ApplicationVariant).outputs.forEach { variantOutput ->
            val manifestUpdater =
                createManifestUpdateTask(bugsnag, project, variant.name, variantOutput)
            manifestUpdater?.let {
                wireManifestUpdaterTask(manifestUpdater, variant.artifacts)
            }
        }
    }
}

internal fun createManifestUpdateTask(
    bugsnag: BugsnagPluginExtension,
    project: Project,
    variantName: String,
    variantOutput: VariantOutput
): TaskProvider<BugsnagManifestUuidTask>? {
    if (!bugsnag.enabled.get()) {
        return null
    }
    val variantFilterImpl = VariantFilterImpl(variantName)
    if (!isVariantEnabled(bugsnag, variantFilterImpl)) {
        return null
    }

    check(variantOutput is VariantOutputImpl)
    val manifestInfoOutputFile = project.computeManifestInfoOutputV2(variantOutput.baseName)
    val buildUuidProvider = project.newUuidProvider()
    return BugsnagManifestUuidTask.register(project, variantOutput) {
        versionCode.set(variantOutput.versionCode)
        versionName.set(variantOutput.versionName)
        buildUuid.set(buildUuidProvider)
        manifestInfoProvider.set(manifestInfoOutputFile)
    }
}

/**
 * Wires the manifest UUID task up so that it transforms each variant's AndroidManifest.
 */
internal fun wireManifestUpdaterTask(
    manifestUpdater: TaskProvider<BugsnagManifestUuidTask>,
    artifacts: Artifacts
) {
    artifacts
        .use(manifestUpdater)
        .wiredWithFiles(
            taskInput = BugsnagManifestUuidTask::inputManifest,
            taskOutput = BugsnagManifestUuidTask::outputManifest
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
