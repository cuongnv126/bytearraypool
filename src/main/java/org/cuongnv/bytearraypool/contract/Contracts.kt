package org.cuongnv.bytearraypool.contract

import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import org.cuongnv.bytearraypool.pool.ByteArrayPool
import org.cuongnv.bytearraypool.stream.RecyclingBufferedOutputStream
import org.cuongnv.bytearraypool.stream.recycling
import org.cuongnv.bytearraypool.stream.recyclingStream
import org.cuongnv.bytearraypool.stream.stream

interface Recyclable {
    fun recycle()
}

fun main() {
    // Create byte[] with size = 16KB.
    val byte16k = ByteArrayPool.getInstance().get(16 * 1024)
    byte16k.use {
        // Do something with 16KB.
    }

// Use as input stream
val inputStream = FileInputStream("./something.txt").recyclingStream()
inputStream.recycling { // auto recycle
    it.stream { data, read, allRead ->
        // Do something on sink of stream.
        return@stream true
    }
}

// Similar with output stream
val outputStream = RecyclingBufferedOutputStream(
    ByteArrayOutputStream()
)

outputStream.use { // auto recycle and close
    // Do something with stream
}

outputStream.recycling { // auto recycle
    // Do something with stream
}
}