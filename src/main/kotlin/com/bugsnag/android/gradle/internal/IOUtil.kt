package com.bugsnag.android.gradle.internal

import okio.buffer
import okio.gzip
import okio.sink
import okio.source
import java.io.File
import java.io.InputStream

/**
 * Outputs the contents of stdout into the gzip file output file
 *
 * @param stdout The input stream
 * @param outputFile The output file
 * included in the output file or not
 */
internal fun outputZipFile(stdout: InputStream, outputFile: File) {
    stdout.source().use { source ->
        outputFile.sink().gzip().buffer().use { gzipSink ->
            gzipSink.writeAll(source)
        }
    }
}
