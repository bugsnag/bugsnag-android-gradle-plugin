package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariantOutput
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class AndroidManifestInfo(
    val apiKey: String,
    val versionCode: String,
    val buildUUID: String,
    val versionName: String,
    val applicationId: String,
    val metaVersionCode: String?,
    val metaVersionName: String?
) : Serializable {
    internal fun write(file: File) {
        file.sink().buffer().use {
            ADAPTER.toJson(it, this)
        }
    }

    internal fun forApkVariantOutput(variant: ApkVariantOutput): AndroidManifestInfo {
        var variantVersionCode = metaVersionCode ?: versionCode
        if (variant.versionCodeOverride.toString() != versionCode) {
            require(metaVersionCode == null) {
                "cannot use versionCodeOverride and com.bugsnag.android.VERSION_CODE meta-data in the same project"
            }

            variantVersionCode = variant.versionCodeOverride.toString()
        }

        var variantVersionName = metaVersionName ?: versionName
        if (variant.versionNameOverride != null && variant.versionNameOverride != versionName) {
            require(metaVersionName == null) {
                "cannot use versionNameOverride and com.bugsnag.android.APP_VERSION meta-data in the same project"
            }

            variantVersionName = variant.versionNameOverride
        }

        return copy(
            versionCode = variantVersionCode,
            versionName = variantVersionName
        )
    }

    internal companion object {
        private const val serialVersionUID = 1L

        private val ADAPTER = Moshi.Builder().build().adapter(AndroidManifestInfo::class.java)

        fun read(file: File): AndroidManifestInfo {
            return file.source().buffer().use {
                ADAPTER.fromJson(it) ?: error("Failed to parse manifest info.")
            }
        }
    }
}
