package org.chorus_oss.chorus.level.format.bitarray

import com.google.common.base.Objects
import kotlin.math.ceil

@JvmRecord
data class Pow2BitArray(override val version: BitArrayVersion, override val size: Int, override val words: IntArray) :
    BitArray {
    override fun set(index: Int, value: Int) {
        val bitIndex = index * version.bits
        val arrayIndex = bitIndex shr 5
        val offset = bitIndex and 31
        words[arrayIndex] =
            words[arrayIndex] and (version.maxEntryValue shl offset).inv() or ((value and version.maxEntryValue) shl offset)
    }

    override fun get(index: Int): Int {
        val bitIndex = index * version.bits
        val arrayIndex = bitIndex shr 5
        val wordOffset = bitIndex and 31
        return words[arrayIndex] ushr wordOffset and version.maxEntryValue
    }

    override fun copy(): BitArray {
        return Pow2BitArray(
            this.version, this.size,
            words.copyOf(words.size)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Pow2BitArray) return false
        return size == other.size && version == other.version && words.contentEquals(other.words)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(version, size, words.contentHashCode())
    }

    init {
        val expectedWordsLength = ceil(size.toFloat() / version.entriesPerWord).toInt()
        require(words.size == expectedWordsLength) {
            "Invalid length given for storage, got: " + words.size +
                    " but expected: " + expectedWordsLength
        }
    }
}