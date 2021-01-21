package com.bugsnag.android.gradle.internal

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.bugsnag.android.gradle.GroovyCompat
import org.gradle.api.Project
import org.gradle.util.VersionNumber
import java.io.File
import java.nio.file.Paths

/**
 * Finds the mapping file locations for DexGuard >=9. This can be different depending on whether it
 * is a bundle or an APK.
 */
internal fun findMappingFileDexguard9(
    project: Project,
    variant: ApkVariant,
    variantOutput: ApkVariantOutput
): List<File> {
    return listOf(
        findDexguardMappingFile(project, variant, variantOutput, "outputs", "dexguard", "mapping", "apk"),
        findDexguardMappingFile(project, variant, variantOutput, "outputs", "dexguard", "mapping", "bundle")
    )
}

/**
 * Finds the mapping file location for DexGuard <9
 */
internal fun findMappingFileDexguardLegacy(
    project: Project,
    variant: ApkVariant,
    variantOutput: ApkVariantOutput
): File {
    return findDexguardMappingFile(project, variant, variantOutput, "outputs", "mapping")
}

/**
 * Retrieves the location of a DexGuard mapping file for the given variantOutput.
 * The expected location for this is:
 * /build/outputs/mapping/<productFlavor>/<buildType>/<split>/mapping.txt
 *
 * variant.mappingFile cannot currently be overridden using the AGP DSL on a per variantOutput
 * basis, which is why the DexGuard plugin sets a different output for its mapping files.
 * see https://issuetracker.google.com/issues/78921539
 */
private fun findDexguardMappingFile(
    project: Project,
    variant: ApkVariant,
    variantOutput: ApkVariantOutput,
    vararg path: String
): File {
    val buildDir = project.buildDir.toString()
    var outputDir = variantOutput.dirName
    if (variantOutput.dirName.endsWith("dpi" + File.separator)) {
        outputDir = File(variantOutput.dirName).parent
        if (outputDir == null) { // if only density splits enabled
            outputDir = ""
        }
    }
    return Paths.get(buildDir, *path, variant.dirName, outputDir, "mapping.txt").toFile()
}

/**
 * Returns true if the DexGuard plugin has been applied to the project
 */
internal fun Project.hasDexguardPlugin(): Boolean {
    return pluginManager.hasPlugin("dexguard")
}

/**
 * Retrieves the major version of DexGuard in use in the project
 */
internal fun getDexguardMajorVersionInt(project: Project): Int {
    val version = GroovyCompat.getDexguardVersionString(project) ?: ""
    val versionNumber = VersionNumber.parse(version)
    return versionNumber.major
}

/**
 * Gets the task name for the Dexguard App Bundle task for this variant.
 */
internal fun getDexguardAabTaskName(variant: ApkVariant): String {
    val buildType = variant.buildType.name.capitalize()
    val flavor = variant.flavorName.capitalize()
    return "dexguardAab$flavor$buildType"
}
