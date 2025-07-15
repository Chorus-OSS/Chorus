package org.chorus_oss.chorus.utils


import io.netty.util.internal.EmptyArrays
import java.nio.charset.StandardCharsets
import kotlin.math.min


class BinaryStream {
    var offset: Int
    var buffer: ByteArray
        private set
    var count: Int
        private set

    constructor() {
        this.buffer = ByteArray(32)
        this.offset = 0
        this.count = 0
    }

    @JvmOverloads
    constructor(buffer: ByteArray, offset: Int = 0) {
        this.buffer = buffer
        this.offset = offset
        this.count = buffer.size
    }

    fun reset(): BinaryStream {
        this.offset = 0
        this.count = 0
        return this
    }

    fun getBufferCopy(): ByteArray {
        return this.buffer.copyOf(this.count)
    }

    fun setBuffer(buffer: ByteArray) {
        this.buffer = buffer
        this.count = buffer.size
    }

    @JvmOverloads
    operator fun get(len: Int = this.count - this.offset): ByteArray {
        var len1 = len
        if (len1 < 0) {
            this.offset = this.count - 1
            return EmptyArrays.EMPTY_BYTES
        }
        len1 = min(len1.toDouble(), (this.count - this.offset).toDouble()).toInt()
        this.offset += len1
        return this.buffer.copyOfRange(this.offset - len1, this.offset)
    }

    fun put(bytes: ByteArray?) {
        if (bytes == null) {
            return
        }

        this.ensureCapacity(this.count + bytes.size)
        bytes.copyInto(this.buffer, this.count)

        this.count += bytes.size
    }

    val lInt: Int
        get() = Binary.readLInt(this[4])

    fun putLShort(s: Int) {
        this.put(Binary.writeLShort(s))
    }

    fun putLFloat(v: Float) {
        this.put(Binary.writeLFloat(v))
    }

    fun putBoolean(bool: Boolean) {
        this.putByte((if (bool) 1 else 0).toByte())
    }

    val byte: Byte
        get() = (buffer[offset++].toInt() and 0xff).toByte()

    fun putByte(b: Byte) {
        this.put(byteArrayOf(b))
    }

    fun putByteArray(b: ByteArray) {
        this.putUnsignedVarInt(b.size.toLong())
        this.put(b)
    }

    fun putString(string: String) {
        val b = string.toByteArray(StandardCharsets.UTF_8)
        this.putByteArray(b)
    }

    val unsignedVarInt: Long
        get() = VarInt.readUnsignedVarInt(this)

    fun putUnsignedVarInt(v: Long) {
        VarInt.writeUnsignedVarInt(this, v)
    }

    fun putVarInt(v: Int) {
        VarInt.writeVarInt(this, v)
    }

    fun putVarLong(v: Long) {
        VarInt.writeVarLong(this, v)
    }

    private fun ensureCapacity(minCapacity: Int) {
        // overflow-conscious code
        if (minCapacity - buffer.size > 0) {
            grow(minCapacity)
        }
    }

    private fun grow(minCapacity: Int) {
        // overflow-conscious code
        val oldCapacity = buffer.size
        var newCapacity = oldCapacity shl 1

        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity
        }

        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity)
        }
        this.buffer = buffer.copyOf(newCapacity)
    }

    companion object : Loggable {
        private const val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8

        private fun hugeCapacity(minCapacity: Int): Int {
            if (minCapacity < 0) { // overflow
                throw OutOfMemoryError()
            }
            return if (minCapacity > MAX_ARRAY_SIZE) Int.MAX_VALUE else MAX_ARRAY_SIZE
        }
    }
}
