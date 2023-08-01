package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.bugsnag.android.gradle.internal.findMappingFileDexguard9
import com.bugsnag.android.gradle.internal.findMappingFileDexguardLegacy
import com.bugsnag.android.gradle.internal.getDexguardMajorVersionInt
import com.bugsnag.android.gradle.internal.hasDexguardPlugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider

/**
 * Creates a Provider which finds the mapping file for a given variantOutput and filters out
 * non-existent outputs.
 */
internal fun createMappingFileProvider(
    project: Project,
    variant: BaseVariant,
    variantOutput: BaseVariantOutput,
    dexguardMajorVersion: Int? = null
): Provider<FileCollection> {
    return findMappingFiles(project, variant, variantOutput, dexguardMajorVersion)
        .map { files -> files.filter { it.exists() } }
}

private fun findMappingFiles(
    project: Project,
    variant: BaseVariant,
    variantOutput: BaseVariantOutput,
    dexguardMajorVersion: Int? = null
): Provider<FileCollection> {
    return when {
        project.hasDexguardPlugin() -> {
            val dexguardVersion = dexguardMajorVersion ?: getDexguardMajorVersionInt(project)
            if (dexguardVersion >= 9) {
                project.provider {
                    val files = findMappingFileDexguard9(project, variant, variantOutput)
                    project.layout.files(files)
                }
            } else {
                project.provider {
                    val file = findMappingFileDexguardLegacy(project, variant, variantOutput)
                    project.layout.files(file)
                }
            }
        }
        else -> {
            findMappingFileAgp(variant, project)
        }
    }
}

/**
 * Use AGP supplied value, preferring the new "getMappingFileProvider" API but falling back
 * to the old "mappingFile" API if necessary
 */
@Suppress("SwallowedException")
internal fun findMappingFileAgp(
    variant: BaseVariant,
    project: Project
) = try {
    variant.mappingFileProvider
} catch (exc: Throwable) {
    project.provider { project.layout.files(variant.mappingFileProvider.orNull) }
}
