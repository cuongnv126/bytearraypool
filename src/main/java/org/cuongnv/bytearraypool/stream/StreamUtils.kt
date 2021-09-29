package org.cuongnv.bytearraypool.stream

import java.io.InputStream

inline fun RecyclingBufferedInputStream.stream(action: (data: ByteArray, read: Int, allRead: Long) -> Boolean) {
    val buf = ByteArray(bufSize)
    var read: Int
    var allRead = 0L
    while (read(buf, 0, bufSize).also { read = it } > 0) {
        allRead += read
        val next = action(buf, read, allRead)
        if (!next) break
    }
}

fun InputStream.recyclingStream(bufferSize: Int = StreamConst.DEFAULT_BUFFER_SIZE): RecyclingBufferedInputStream =
    if (this is RecyclingBufferedInputStream) {
        this
    } else {
        RecyclingBufferedInputStream(
            this,
            bufferSize
        )
    }

object StreamConst {
    const val DEFAULT_BUFFER_SIZE = 4 * 1024

    @JvmStatic
    val NBUF_DEFAULT_SIZE = 10 * 1024
}
