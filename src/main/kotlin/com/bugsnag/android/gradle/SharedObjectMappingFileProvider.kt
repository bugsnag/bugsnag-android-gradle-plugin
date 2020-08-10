package com.bugsnag.android.gradle

import com.android.build.VariantOutput
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import java.io.File

fun getSearchDirectories(project: Project,
                         variant: ApkVariant): ConfigurableFileCollection {
    val bugsnag = project.extensions.getByType(BugsnagPluginExtension::class.java)
    val searchDirectories = bugsnag.sharedObjectPaths.get().toMutableSet()

    resolveExternalNativeBuildTasks(variant).forEach { task ->
        searchDirectories.add(task.objFolder)
        searchDirectories.add(task.soFolder)
    }
    return project.files(searchDirectories.toList())
}

fun findSharedObjectMappingFiles(project: Project,
                                 variantOutput: ApkVariantOutput,
                                 searchDirectories: List<File>): Collection<File> {
    val symbolPath = findSymbolPath(variantOutput)
    project.logger.info("Bugsnag: using symbolPath $symbolPath")
    val splitArch = variantOutput.getFilter(VariantOutput.FilterType.ABI)

    return searchDirectories.flatMap { findSharedObjectFiles(it, splitArch) }
        .toSet() // dedupe SO files
        .toList()
        .sortedBy { it.parentFile.name } // sort SO files alphabetically by architecture for consistent request order
}

private fun findSymbolPath(variantOutput: ApkVariantOutput?): File {
    val resources = variantOutput!!.processResourcesProvider.get()
    return resources.property("textSymbolOutputFile") as? File
        ?: throw IllegalStateException("Could not find symbol path")
}

private fun resolveExternalNativeBuildTasks(variant: ApkVariant): Collection<ExternalNativeBuildTask> {
    return variant.externalNativeBuildProviders.mapNotNull { it.orNull }
}

/**
 * Searches the subdirectories of a given path for SO files. These are added to a
 * collection and returned if they should be uploaded by the current task.
 *
 * If the variantOutput is an APK split the splitArch parameter should be non-null,
 * as this allows the avoidance of unnecessary uploads of all architectures for each split.
 *
 * @param searchDirectory The parent path to search. Each subdirectory should
 * represent an architecture
 * @param abiArchitecture The architecture of the ABI split, or null if this is not an APK split.
 */
private fun findSharedObjectFiles(searchDirectory: File,
                                  abiArchitecture: String?): Collection<File> {
    val sharedObjectFiles = mutableSetOf<File>()
    if (searchDirectory.exists() && searchDirectory.isDirectory) {
        searchDirectory.listFiles()
            .filter { archDir -> archDir.exists() && archDir.isDirectory }
            .filter { archDir -> abiArchitecture == null || archDir.name == abiArchitecture }
            .forEach {
                val archFiles = it.listFiles()
                    .filter { file -> file.name.endsWith(".so") }
                sharedObjectFiles.addAll(archFiles)
            }
    }
    return sharedObjectFiles
}

