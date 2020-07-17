package com.bugsnag.android.gradle

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token.NULL
import com.squareup.moshi.JsonWriter
import okio.buffer
import okio.sink
import okio.source
import java.io.File

data class AndroidManifestInfo(
    var apiKey: String,
    var versionCode: String,
    var buildUUID: String,
    var versionName: String
) {
    internal fun write(file: File) {
        file.sink().buffer().use {
            ADAPTER.toJson(it, this)
        }
    }

    internal companion object {
        private val OPTIONS = JsonReader.Options.of(
            "apiKey",
            "versionCode",
            "buildUUID",
            "versionName"
        )

        private val ADAPTER = object : JsonAdapter<AndroidManifestInfo>() {
            override fun fromJson(reader: JsonReader): AndroidManifestInfo? {
                if (reader.peek() == NULL) {
                    return reader.nextNull()
                }
                lateinit var apiKey: String
                lateinit var versionCode: String
                lateinit var buildUUID: String
                lateinit var versionName: String
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.selectName(OPTIONS)) {
                        0 -> apiKey = reader.nextString()
                        1 -> versionCode = reader.nextString()
                        2 -> buildUUID = reader.nextString()
                        3 -> versionName = reader.nextString()
                        -1 -> reader.skipValue()
                    }
                }
                reader.endObject()
                return AndroidManifestInfo(apiKey, versionCode, buildUUID, versionName)
            }

            override fun toJson(writer: JsonWriter, value: AndroidManifestInfo?) {
                if (value == null) {
                    writer.nullValue()
                    return
                }
                writer.beginObject()
                    .name("apiKey")
                    .value(value.apiKey)
                    .name("versionCode")
                    .value(value.versionCode)
                    .name("buildUUID")
                    .value(value.buildUUID)
                    .name("versionName")
                    .value(value.versionName)
                    .endObject()
            }

        }

        fun read(file: File): AndroidManifestInfo {
            return file.source().buffer().use {
                ADAPTER.fromJson(it) ?: error("Failed to parse manifest info.")
            }
        }
    }
}
