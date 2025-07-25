package org.chorus_oss.chorus.level.format.bitarray

import io.netty.buffer.ByteBuf

class SingletonBitArray : BitArray {
    override fun set(index: Int, value: Int) {
        throw UnsupportedOperationException()
    }

    override fun get(index: Int): Int {
        return 0
    }

    override fun writeSizeToNetwork(buffer: ByteBuf, size: Int) {
        throw UnsupportedOperationException()
    }

    override val size: Int
        get() {
            return 1
        }

    override val words: IntArray
        get() {
            return EMPTY_ARRAY
        }

    override val version: BitArrayVersion
        get() {
            return BitArrayVersion.V0
        }

    override fun copy(): SingletonBitArray {
        return SingletonBitArray()
    }

    companion object {
        private val EMPTY_ARRAY = IntArray(0)
    }
}
