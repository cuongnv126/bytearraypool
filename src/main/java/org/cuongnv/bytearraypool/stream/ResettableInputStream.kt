package org.cuongnv.bytearraypool.stream

import java.io.FilterInputStream
import java.io.InputStream
import org.cuongnv.bytearraypool.contract.Recyclable

/**
 * Created by cuongnv on Jul 29, 2021
 */

internal class ResettableInputStream(
    source: InputStream
) : FilterInputStream(
    if (source.markSupported()) source else source.recyclingStream()
), Recyclable {

    override fun recycle() {
        val stream = `in` ?: return
        if (stream is Recyclable) stream.recycle()
    }
}

internal fun InputStream.asResettable(): ResettableInputStream {
    return if (this is ResettableInputStream) this else ResettableInputStream(this)
}

inline fun <T : Recyclable, R> T.recycling(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        recycle()
    }
}