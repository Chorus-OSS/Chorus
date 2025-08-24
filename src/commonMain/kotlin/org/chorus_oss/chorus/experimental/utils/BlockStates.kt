package org.chorus_oss.chorus.experimental.utils

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.nbt.Tag
import org.chorus_oss.nbt.TagSerialization
import org.chorus_oss.nbt.tags.ByteTag
import org.chorus_oss.nbt.tags.CompoundTag
import org.chorus_oss.nbt.tags.IntTag
import org.chorus_oss.nbt.tags.StringTag

object BlockStates {
    fun getHash(identifier: String, state: Map<String, Any>): Int {
        if (identifier == BlockID.UNKNOWN) {
            return -2 // This is special case
        }

        val states = CompoundTag(state.entries.associate { (identifier, value) ->
            identifier to when (value) {
                is Int -> IntTag(value)
                is String -> StringTag(value)
                is Boolean -> ByteTag(if (value) 1 else 0)
                else -> throw IllegalArgumentException()
            }
        })

        val tag = CompoundTag(
            mapOf(
                "name" to StringTag(identifier),
                "states" to states
            )
        )

        return Buffer().run {
            Tag.serialize(tag, this, TagSerialization.LE, isRoot = true)
            `FNV1A-32`(this.readByteArray())
        }
    }

    fun getTag(
        identifier: String, state: Map<String, Any>
    ): CompoundTag {
        val states = CompoundTag(state.entries.associate { (identifier, value) ->
            identifier to when (value) {
                is Int -> IntTag(value)
                is String -> StringTag(value)
                is Boolean -> ByteTag(if (value) 1 else 0)
                else -> throw IllegalArgumentException()
            }
        })

        return CompoundTag(
            mapOf(
                "name" to StringTag(identifier),
                "states" to states,
                "version" to IntTag(ProtocolInfo.BLOCK_STATE_VERSION)
            )
        )
    }

    // TODO: remove
    @Suppress("FunctionName")
    fun `FNV1A-32`(data: ByteArray): Int {
        var hash = FNV1_32_INIT
        for (datum in data) {
            hash = hash xor (datum.toInt() and 0xff)
            hash *= FNV1_PRIME_32
        }
        return hash
    }

    private const val FNV1_32_INIT = -0x7ee3623b
    private const val FNV1_PRIME_32 = 0x01000193
}