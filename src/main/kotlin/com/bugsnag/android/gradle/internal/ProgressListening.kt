package com.bugsnag.android.gradle.internal

import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer

/** Simple interface for listening to progress information. */
internal interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}

/** A delegating [RequestBody] that updates a [progressListener] as it uploads. */
internal class ProgressRequestBody(
    private val delegateRequestBody: RequestBody,
    private val progressListener: ProgressListener
) : RequestBody() {

    override fun contentType() = delegateRequestBody.contentType()

    override fun writeTo(sink: BufferedSink) {
        object : ForwardingSink(sink) {
            var totalBytesRead = 0L
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                totalBytesRead += if (byteCount != -1L) byteCount else 0
                progressListener.update(
                    bytesRead = totalBytesRead,
                    contentLength = delegateRequestBody.contentLength(),
                    done = totalBytesRead >= delegateRequestBody.contentLength()
                )
            }
        }.buffer().use(delegateRequestBody::writeTo)
    }

    override fun contentLength() = delegateRequestBody.contentLength()
}
