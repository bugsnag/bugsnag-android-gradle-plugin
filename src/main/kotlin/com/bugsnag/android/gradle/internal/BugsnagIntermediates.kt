package com.bugsnag.android.gradle.internal

import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File

/**
 * Intermediate path where libunity and other Unity SO files are copied
 * after being extracted from the Gzip archive
 */
internal const val UNITY_SO_COPY_DIR = "intermediates/bugsnag/unitySoFiles"

/**
 * Directory where SO files are extracted from bugsnag-android AARs
 */
internal const val JNI_LIBS_DIR = "intermediates/bugsnag-libs"

/**
 * Intermediate directory where NDK SO mapping files are generated
 */
internal const val NDK_SO_MAPPING_DIR = "intermediates/bugsnag/soMappings/ndk"

/**
 * Intermediate directory where NDK SO mapping files are generated
 */
internal const val UNITY_SO_MAPPING_DIR = "intermediates/bugsnag/soMappings/unity"

/**
 * Gets a unique suffix for a [BaseVariantOutput] which is used in tasks and intermediate directories
 */
internal fun BaseVariantOutput.taskNameSuffix() = name.capitalize()

/** Intermediate locations for task outputs/inputs **/

internal fun intermediateForUnitySoRequest(project: Project, output: BaseVariantOutput): Provider<RegularFile> {
    val path = "intermediates/bugsnag/requests/unityFor${output.taskNameSuffix()}.json"
    return project.layout.buildDirectory.file(path)
}

internal fun intermediateForReleaseRequest(project: Project, output: BaseVariantOutput): Provider<RegularFile> {
    val path = "intermediates/bugsnag/requests/releasesFor${output.taskNameSuffix()}.json"
    return project.layout.buildDirectory.file(path)
}

internal fun intermediateForNdkSoRequest(project: Project, output: BaseVariantOutput): Provider<RegularFile> {
    val path = "intermediates/bugsnag/requests/ndkFor${output.taskNameSuffix()}.json"
    return project.layout.buildDirectory.file(path)
}

internal fun intermediateForMappingFileRequest(project: Project, output: BaseVariantOutput): Provider<RegularFile> {
    val path = "intermediates/bugsnag/requests/proguardFor${output.taskNameSuffix()}.json"
    return project.layout.buildDirectory.file(path)
}

internal fun intermediateForGenerateJvmMapping(project: Project, output: BaseVariantOutput): Provider<RegularFile> {
    val path = "intermediates/bugsnag/jvmMappings/mappingFor${output.taskNameSuffix()}.gz"
    return project.layout.buildDirectory.file(path)
}

internal fun intermediateForUploadSourcemaps(project: Project, output: BaseVariantOutput): Provider<RegularFile> {
    val path = "intermediates/bugsnag/requests/sourceMapFor${output.taskNameSuffix()}"
    return project.layout.buildDirectory.file(path)
}

internal fun intermediateForRescuedReactNativeBundle(project: Project, output: BaseVariantOutput): File {
    val path = "intermediates/bugsnag/rescued/${output.taskNameSuffix()}.android.bundle"
    return project.layout.buildDirectory.file(path).get().asFile
}

internal fun Project.computeManifestInfoOutputV2(variant: String): Provider<RegularFile> {
    val path = "intermediates/bugsnag/manifestInfoFor${variant.capitalize()}.json"
    return layout.buildDirectory.file(path)
}

internal fun Project.computeManifestInfoOutputV1(
    output: BaseVariantOutput
): Provider<RegularFile> {
    val path = "intermediates/bugsnag/manifestInfoFor${output.taskNameSuffix()}.json"
    return layout.buildDirectory.file(path)
}
