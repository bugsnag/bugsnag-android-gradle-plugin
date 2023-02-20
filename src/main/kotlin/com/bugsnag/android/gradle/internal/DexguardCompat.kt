package com.bugsnag.android.gradle.internal

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.bugsnag.android.gradle.GroovyCompat
import org.gradle.api.Project
import org.semver.Version
import java.io.File
import java.nio.file.Paths

/**
 * Finds the mapping file locations for DexGuard >=9. This can be different depending on whether it
 * is a bundle or an APK.
 */
internal fun findMappingFileDexguard9(
    project: Project,
    variant: BaseVariant,
    variantOutput: BaseVariantOutput
): List<File> {
    return listOf(
        findDexguardMappingFile(project, variant, variantOutput, arrayOf("outputs", "dexguard", "mapping", "apk")),
        findDexguardMappingFile(project, variant, variantOutput, arrayOf("outputs", "dexguard", "mapping", "bundle"))
    )
}

/**
 * Finds the mapping file location for DexGuard <9
 */
internal fun findMappingFileDexguardLegacy(
    project: Project,
    variant: BaseVariant,
    variantOutput: BaseVariantOutput
): File {
    return findDexguardMappingFile(project, variant, variantOutput, arrayOf("outputs", "mapping"))
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
    variant: BaseVariant,
    variantOutput: BaseVariantOutput,
    path: Array<String>
): File {
    val buildDir = project.buildDir.toString()
    var outputDir = variantOutput.dirName
    // Don't account for splits in bundles
    if (path[path.size - 1] == "bundle") {
        outputDir = ""
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
 * Determine whether a specific variant is configured for DexGuard.
 */
internal fun Project.isDexguardEnabledForVariant(variant: BaseVariant): Boolean {
    val flavor = variant.flavorName
    val buildType = if (flavor.isEmpty()) variant.buildType.name else variant.buildType.name.capitalize()
    return GroovyCompat.isDexguardEnabledForVariant(project, "$flavor$buildType")
}

/**
 * Retrieves the major version of DexGuard in use in the project
 */
internal fun getDexguardVersion(project: Project): Version? {
    val version = GroovyCompat.getDexguardVersionString(project) ?: return null
    return Version.parse(version)
}

/**
 * Gets the task name for the Dexguard App Bundle task for this variant.
 */
internal fun getDexguardAabTaskName(variant: BaseVariant): String {
    val buildType = variant.buildType.name.capitalize()
    val flavor = variant.flavorName.capitalize()
    return "dexguardAab$flavor$buildType"
}
