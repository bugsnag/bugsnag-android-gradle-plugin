package com.bugsnag.android.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.bugsnag.android.gradle.internal.hasDexguardPlugin
import com.bugsnag.android.gradle.internal.hasMultipleOutputs
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File
import java.nio.file.Paths

/**
 * Creates a Provider which finds the mapping file for a given variantOutput and filters out
 * non-existent outputs.
 */
internal fun createMappingFileProvider(
    project: Project,
    variant: ApkVariant,
    variantOutput: ApkVariantOutput,
    android: AppExtension
): Provider<FileCollection> {
    return findMappingFiles(project, variant, variantOutput, android)
        .map { files -> files.filter { it.exists() } }
}

private fun findMappingFiles(
    project: Project,
    variant: ApkVariant,
    variantOutput: ApkVariantOutput,
    android: AppExtension
): Provider<FileCollection> {
    if (project.hasDexguardPlugin() && android.hasMultipleOutputs()) {
        val mappingFile = findDexguardMappingFile(project, variant, variantOutput)
        if (mappingFile.exists()) {
            return project.provider { project.layout.files(mappingFile) }
        } else {
            project.logger.warn("Bugsnag: Could not find DexGuard mapping file at: $mappingFile -" +
                " falling back to AGP mapping file value")
        }
    }

    // Use AGP supplied value, preferring the new "getMappingFileProvider" API but falling back
    // to the old "mappingFile" API if necessary
    return try {
        variant.mappingFileProvider
    } catch (exc: Throwable) {
        project.provider { project.layout.files(variant.mappingFile) }
    }
}

/**
 * Retrieves the location of a DexGuard mapping file for the given variantOutput. The expected location for this
 * is: build/outputs/mapping/<productFlavor>/<buildType>/<split>
 *
 * variant.mappingFile cannot currently be overridden using the AGP DSL on a per-variantOutput basis, which
 * necessitates this workaround. https://issuetracker.google.com/issues/78921539
 */
private fun findDexguardMappingFile(project: Project,
                                    variant: ApkVariant,
                                    variantOutput: ApkVariantOutput): File {
    val buildDir = project.buildDir.toString()
    var outputDir = variantOutput.dirName
    if (variantOutput.dirName.endsWith("dpi" + File.separator)) {
        outputDir = File(variantOutput.dirName).parent
        if (outputDir == null) { // if only density splits enabled
            outputDir = ""
        }
    }
    return Paths.get(buildDir, "outputs", "mapping", variant.dirName, outputDir, "mapping.txt").toFile()
}
