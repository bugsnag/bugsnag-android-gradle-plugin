package com.bugsnag.android.gradle.internal

import com.android.build.gradle.api.BaseVariantOutput

/** Names of bugsnag gradle tasks **/

internal const val TASK_JNI_LIBS = "bugsnagInstallJniLibsTask"

internal fun taskNameForGenerateJvmMapping(output: BaseVariantOutput) =
    "generateBugsnag${output.taskNameSuffix()}Mapping"

internal fun taskNameForUploadJvmMapping(output: BaseVariantOutput) =
    "uploadBugsnag${output.taskNameSuffix()}Mapping"

internal fun taskNameForGenerateNdkMapping(output: BaseVariantOutput) =
    "generateBugsnagNdk${output.taskNameSuffix()}Mapping"

internal fun taskNameForGenerateUnityMapping(output: BaseVariantOutput) =
    "generateBugsnagUnity${output.taskNameSuffix()}Mapping"

internal fun taskNameForUploadNdkMapping(output: BaseVariantOutput) =
    "uploadBugsnagNdk${output.taskNameSuffix()}Mapping"

internal fun taskNameForUploadUnityMapping(output: BaseVariantOutput) =
    "uploadBugsnagUnity${output.taskNameSuffix()}Mapping"

internal fun taskNameForUploadRelease(output: BaseVariantOutput) =
    "bugsnagRelease${output.taskNameSuffix()}Task"

internal fun taskNameForManifestUuid(variantOutput: String) =
    "processBugsnag${variantOutput.capitalize()}Manifest"

internal fun taskNameForUploadSourcemaps(output: BaseVariantOutput) =
    "uploadBugsnag${output.taskNameSuffix()}SourceMaps"
