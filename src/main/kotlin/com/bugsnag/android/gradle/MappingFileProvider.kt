package com.bugsnag.android.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File
import java.nio.file.Paths

/**
 * Creates a Provider which finds the mapping file for a given variantOutput.
 */
fun createMappingFileProvider(project: Project,
                              variant: ApkVariant,
                              variantOutput: ApkVariantOutput): Provider<RegularFile> {
    val fileProvider: Provider<File> = project.provider {
        val mappingFile = findMappingFile(project, variant, variantOutput)
        val logger = project.logger
        logger.info("Bugsnag: Using mapping file: $mappingFile")

        // If we haven't enabled proguard for this variant, or the proguard
        // configuration includes -dontobfuscate, the mapping file
        // will not exist (but we also won't need it).
        if (mappingFile == null) {
            throw IllegalStateException("Mapping file not found: $mappingFile")
        }
        mappingFile
    }
    return project.layout.file(fileProvider)
}

private fun findMappingFile(project: Project,
                            variant: ApkVariant,
                            variantOutput: ApkVariantOutput): File? {
    val plugin = project.plugins.getPlugin(BugsnagPlugin::class.java)
    val android = project.extensions.getByType(AppExtension::class.java)
    if (plugin.hasDexguardPlugin(project) && plugin.hasMultipleOutputs(android)) {
        val mappingFile = findDexguardMappingFile(project, variant, variantOutput)
        if (mappingFile.exists()) {
            return mappingFile
        } else {
            project.logger.warn("Bugsnag: Could not find DexGuard mapping file at: $mappingFile -" +
                " falling back to AGP mapping file value")
        }
    }

    // Use AGP supplied value, preferring the new "getMappingFileProvider" API but falling back
    // to the old "mappingFile" API if necessary
    return try {
        val mappingFileProvider = variant.mappingFileProvider.orNull

        // We will warn about not finding a mapping file later, so there's no need to warn here
        if (mappingFileProvider == null || mappingFileProvider.isEmpty) {
            null
        } else {
            mappingFileProvider.singleFile
        }
    } catch (exc: Throwable) {
        variant.mappingFile
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
