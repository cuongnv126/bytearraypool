package org.cuongnv.bytearraypool.pool

import java.util.concurrent.locks.ReentrantLock

/**
 * Created by cuongnv on Aug 07, 2021
 */

class ByteArrayPool(
    private val maxSize: Int
) {
    private val safeLock = ReentrantLock()
    private val map = LinkedHashMap<Int, GroupPooledByteArray>(0, 0.75f, true)

    @Volatile
    private var tokenLoop = Int.MIN_VALUE

    @Volatile
    private var size: Int = 0

    /**
     * Tokenize [PooledByteArray] to target, it's call inside synchronized block outsize,
     * So we don't care about lost state of [tokenLoop].
     */
    private fun tokenize(item: PooledByteArray): RecyclingByteArray {
        // Create new token
        val token = ++tokenLoop
        if (tokenLoop == Int.MAX_VALUE) tokenLoop = Int.MIN_VALUE

        // Acquire token to [RecyclingByteArray]
        return RecyclingByteArray(item).also { it.acquire(token) }
    }

    /**
     * Try to find reuse [PooledByteArray] and [tokenize] it to target.
     *
     * After that succeed called, [PooledByteArray] will in state acquired and no other one can touch to them.
     * Please call [RecyclingByteArray.release] after done.
     */
    fun get(byteSize: Int): RecyclingByteArray {
        val newRecyclingItem: RecyclingByteArray
        safeLock.lock()
        try {
            // Try reuse
            val reuseItem = map[byteSize]?.opt()
            if (reuseItem != null) return tokenize(reuseItem)

            // Create new
            val mapValue = map.getOrPut(byteSize) { GroupPooledByteArray(byteSize) }
            val newItem = mapValue.create()
            size += newItem.byteArray.size
            newRecyclingItem = tokenize(newItem)
        } finally {
            safeLock.unlock()
        }

        trimToSize(maxSize)

        return newRecyclingItem
    }

    /**
     * Throw the [ByteArray] inside [GroupPooledByteArray] when [ByteArrayPool] out of [maxSize]
     */
    private fun trimToSize(maxSize: Int) {
        while (true) {
            var key: Int
            var value: GroupPooledByteArray

            safeLock.lock()
            try {
                check(!(size < 0 || map.isEmpty() && size != 0)) { ("${javaClass.name}.sizeOf() is reporting inconsistent results!") }

                if (size <= maxSize || map.isEmpty()) break

                val toEvict = map.entries.iterator().next()
                value = toEvict.value
                key = toEvict.key

                while (true) {
                    if (size <= maxSize) break

                    val removed = value.remove()
                    size -= removed.byteArray.size
                }

                if (value.isEmpty()) map.remove(key)
            } finally {
                safeLock.unlock()
            }
        }
    }

    companion object {
        private const val MAX_SIZE = 4 * 1024 * 1024 // 6MB

        @Volatile
        private var instance: ByteArrayPool? = null

        @JvmStatic
        fun getInstance(): ByteArrayPool {
            if (instance == null) {
                synchronized(ByteArrayPool::class) {
                    if (instance == null) {
                        instance = ByteArrayPool(MAX_SIZE)
                    }
                }
            }
            return instance!!
        }
    }
}