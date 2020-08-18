package com.bugsnag.android.gradle

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okio.buffer
import okio.sink
import okio.source
import java.io.File

@JsonClass(generateAdapter = true)
data class AndroidManifestInfo(
    val apiKey: String,
    val versionCode: String,
    val buildUUID: String,
    val versionName: String,
    val applicationId: String
) {
    internal fun write(file: File) {
        file.sink().buffer().use {
            ADAPTER.toJson(it, this)
        }
    }

    internal companion object {
        private val ADAPTER = Moshi.Builder().build().adapter(AndroidManifestInfo::class.java)

        fun read(file: File): AndroidManifestInfo {
            return file.source().buffer().use {
                ADAPTER.fromJson(it) ?: error("Failed to parse manifest info.")
            }
        }
    }
}
