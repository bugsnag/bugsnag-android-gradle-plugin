package com.bugsnag.android.gradle

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

    internal fun withOverrides(
        versionCodeOverride: Int? = null,
        versionNameOverride: String? = null
    ): AndroidManifestInfo {
        return copy(
            versionCode = metaVersionCode ?: versionCodeOverride?.toString() ?: versionCode,
            versionName = metaVersionName ?: versionNameOverride ?: versionName
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
