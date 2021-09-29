package org.cuongnv.bytearraypool.pool

import java.io.Closeable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by cuongnv on Aug 07, 2021
 */

class PooledByteArray(val byteArray: ByteArray) {
    internal val isUse: AtomicBoolean = AtomicBoolean(false)
    private val token = AtomicInteger(0)


    fun acquire(token: Int) {
        this.token.set(token)
        this.isUse.set(true)
    }

    fun release(token: Int) {
        if (this.token.get() == token) {
            this.isUse.set(false)
        }
    }
}

/**
 * Support multiple [PooledByteArray] to use in multiple thread.
 */
class GroupPooledByteArray(
    private val key: Int
) : LinkedBlockingQueue<PooledByteArray>() {

    /**
     * Try to find reuse [PooledByteArray]
     */
    fun opt(): PooledByteArray? {
        val iterator = iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (!next.isUse.get()) return next
        }
        return null
    }

    /**
     * Create new [PooledByteArray]
     */
    fun create(): PooledByteArray {
        val newItem = PooledByteArray(ByteArray(key))
        add(newItem)
        return newItem
    }
}

/**
 * Wrap [PooledByteArray] to unlock safe for multiple reuse case.
 */
class RecyclingByteArray(
    private val pooledByteArray: PooledByteArray
) : Closeable {
    private var token: Int = 0
    private var isReleased = true

    /**
     * Re-direct byteArray from [PooledByteArray] to this instance.
     */
    val byteArray: ByteArray
        get() {
            if (isReleased) throw IllegalStateException("Byte array was released!")
            return pooledByteArray.byteArray
        }

    /**
     * Try to acquire to current [PooledByteArray]
     */
    fun acquire(token: Int) {
        if (pooledByteArray.isUse.get()) throw IllegalStateException("Pooled byte array was acquired!")

        this.token = token
        pooledByteArray.acquire(token)

        isReleased = false
    }

    /**
     * Release [PooledByteArray] when done.
     */
    fun release() {
        isReleased = true

        pooledByteArray.release(token)
    }

    /**
     * [release]
     */
    override fun close() {
        release()
    }
}