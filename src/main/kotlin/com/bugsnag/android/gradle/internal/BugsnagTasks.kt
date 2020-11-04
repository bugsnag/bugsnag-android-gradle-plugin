package com.bugsnag.android.gradle.internal

import com.android.build.gradle.api.ApkVariantOutput

/** Names of bugsnag gradle tasks **/

internal const val TASK_JNI_LIBS = "bugsnagInstallJniLibsTask"

internal fun taskNameForGenerateJvmMapping(output: ApkVariantOutput) =
    "generateBugsnag${output.taskNameSuffix()}Mapping"

internal fun taskNameForUploadJvmMapping(output: ApkVariantOutput) =
    "uploadBugsnag${output.taskNameSuffix()}Mapping"

internal fun taskNameForGenerateNdkMapping(output: ApkVariantOutput) =
    "generateBugsnagNdk${output.taskNameSuffix()}Mapping"

internal fun taskNameForGenerateUnityMapping(output: ApkVariantOutput) =
    "generateBugsnagUnity${output.taskNameSuffix()}Mapping"

internal fun taskNameForUploadNdkMapping(output: ApkVariantOutput) =
    "uploadBugsnagNdk${output.taskNameSuffix()}Mapping"

internal fun taskNameForUploadUnityMapping(output: ApkVariantOutput) =
    "uploadBugsnagUnity${output.taskNameSuffix()}Mapping"

internal fun taskNameForUploadRelease(output: ApkVariantOutput) =
    "bugsnagRelease${output.taskNameSuffix()}Task"

internal fun taskNameForManifestUuid(variant: String) =
    "processBugsnag${variant.capitalize()}Manifest"

internal fun taskNameForUploadSourcemaps(output: ApkVariantOutput) =
    "uploadBugsnag${output.taskNameSuffix()}SourceMaps"
