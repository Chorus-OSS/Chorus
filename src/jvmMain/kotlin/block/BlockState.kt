package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.Block.Companion.get
import org.chorus_oss.chorus.block.property.type.BlockPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.CompoundTagView
import org.chorus_oss.chorus.registry.Registries
import org.jetbrains.annotations.UnmodifiableView

interface BlockState {
    val identifier: String

    fun blockStateHash(): Int

    fun specialValue(): Short

    fun unsignedBlockStateHash(): Long

    val blockPropertyValues: @UnmodifiableView MutableList<BlockPropertyType.BlockPropertyValue<*, *, *>>

    val blockStateTag: @UnmodifiableView CompoundTagView

    fun <DATATYPE, PROPERTY : BlockPropertyType<DATATYPE>> getPropertyValue(p: PROPERTY): DATATYPE

    fun <DATATYPE, PROPERTY : BlockPropertyType<DATATYPE>> setPropertyValue(
        properties: BlockProperties,
        property: PROPERTY,
        value: DATATYPE
    ): BlockState

    fun setPropertyValue(
        properties: BlockProperties,
        propertyValue: BlockPropertyType.BlockPropertyValue<*, *, *>
    ): BlockState

    fun setPropertyValues(
        properties: BlockProperties,
        vararg values: BlockPropertyType.BlockPropertyValue<*, *, *>
    ): BlockState

    val isDefaultState: Boolean
        get() = Registries.BLOCK.getBlockProperties(identifier).defaultState === this

    fun toBlock(): Block {
        return get(this)
    }

    fun toBlock(locator: Locator): Block {
        return get(this, locator)
    }

    fun toItem(): Item {
        return ItemBlock(this)
    }

    companion object {
        fun makeUnknownBlockState(hash: Int, blockTag: CompoundTag): BlockState {
            return BlockStateImpl.makeUnknownBlockState(hash, blockTag)
        }

        fun computeSpecialValue(propertyValues: MutableList<BlockPropertyType.BlockPropertyValue<*, *, *>>): Short {
            var specialValueBits: Byte = 0
            for (value in propertyValues) specialValueBits =
                (specialValueBits + (value.propertyType.bitSize)).toByte()
            return computeSpecialValue(specialValueBits, propertyValues)
        }

        fun computeSpecialValue(
            specialValueBits: Byte,
            propertyValues: MutableList<BlockPropertyType.BlockPropertyValue<*, *, *>>
        ): Short {
            var specialValueBits1 = specialValueBits
            var specialValue: Short = 0
            for (value in propertyValues) {
                specialValue =
                    (specialValue.toInt() or (value.getIndex() shl (specialValueBits1 - value.propertyType.bitSize)).toShort()
                        .toInt()).toShort()
                specialValueBits1 = (specialValueBits1 - value.propertyType.bitSize).toByte()
            }
            return specialValue
        }
    }
}
