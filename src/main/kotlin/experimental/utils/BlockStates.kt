package org.chorus_oss.chorus.experimental.utils

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockStateImpl
import org.chorus_oss.chorus.block.property.type.BlockPropertyType
import org.chorus_oss.chorus.block.property.type.BlockPropertyType.BlockPropertyValue
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.LinkedCompoundTag
import org.chorus_oss.chorus.nbt.tag.TreeMapCompoundTag
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.utils.HashUtils.fnv1a_32_nbt

object BlockStates {
    fun getDefaultState(identifier: String, propertyTypeList: List<BlockPropertyType<*>>): BlockStateImpl {
        return BlockStateImpl(identifier, propertyTypeList.map { it.createDefaultValue() })
    }

    fun getAllStates(identifier: String, propertyTypeList: List<BlockPropertyType<*>>): List<BlockStateImpl> {
        return propertyTypeList
            .fold(listOf(listOf<BlockPropertyValue<*, *, *>>())) { acc, property ->
                acc.flatMap { list ->
                    property.validValues.map {
                        list + (property.tryCreateValue(it)!!)
                    }
                }
            }
            .map { BlockStateImpl(identifier, it) }
    }

    fun getHash(identifier: String, state: Map<String, Any>): Int {
        if (identifier == BlockID.UNKNOWN) {
            return -2 // This is special case
        }

        val states = TreeMapCompoundTag()
        for ((identifier, value) in state) {
            when (value) {
                is Int -> states.putInt(
                    identifier, value
                )

                is String -> states.putString(
                    identifier, value
                )

                is Boolean -> states.putByte(
                    identifier, if (value) 1 else 0
                )
            }
        }

        val tag = CompoundTag().putString("name", identifier).putCompound("states", states)
        return fnv1a_32_nbt(tag)
    }

    fun getTag(
        identifier: String, state: Map<String, Any>
    ): CompoundTag {
        val states = TreeMapCompoundTag()
        for ((identifier, value) in state) {
            when (value) {
                is Int -> states.putInt(
                    identifier, value
                )

                is String -> states.putString(
                    identifier, value
                )

                is Boolean -> states.putByte(
                    identifier, if (value) 1 else 0
                )
            }
        }

        return LinkedCompoundTag().apply {
            putString("name", identifier)
            putCompound("states", states)
            putInt("version", ProtocolInfo.BLOCK_STATE_VERSION_NO_REVISION)
        }
    }
}