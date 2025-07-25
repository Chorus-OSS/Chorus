package org.chorus_oss.chorus.level.format.bitarray

import io.netty.buffer.ByteBuf
import org.chorus_oss.chorus.utils.ByteBufVarInt

interface BitArray {
    operator fun set(index: Int, value: Int)

    operator fun get(index: Int): Int

    fun writeSizeToNetwork(buffer: ByteBuf, size: Int) {
        ByteBufVarInt.writeInt(buffer, size)
    }

    fun readSizeFromNetwork(buffer: ByteBuf): Int {
        return ByteBufVarInt.readInt(buffer)
    }

    val size: Int

    val words: IntArray

    val version: BitArrayVersion

    fun copy(): BitArray
}