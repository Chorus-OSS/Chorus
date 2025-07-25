package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.type.BlockPropertyType
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.CompoundTagView
import org.chorus_oss.chorus.nbt.tag.LinkedCompoundTag
import org.chorus_oss.chorus.nbt.tag.TreeMapCompoundTag
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.utils.HashUtils
import org.jetbrains.annotations.UnmodifiableView

class BlockStateImpl(
    override val identifier: String,
    val blockHash: Int,
    private val inSpecialValue: Short,
    override val blockPropertyValues: @UnmodifiableView MutableList<BlockPropertyType.BlockPropertyValue<*, *, *>>,
    override val blockStateTag: CompoundTagView
) : BlockState {
    constructor(
        identifier: String,
        blockStateHash: Int,
        propertyValues: MutableList<BlockPropertyType.BlockPropertyValue<*, *, *>>
    ) : this(
        identifier.intern(),
        blockStateHash,
        BlockState.computeSpecialValue(propertyValues),
        propertyValues,
        buildBlockStateTag(identifier, propertyValues)
    )

    constructor(identifier: String, propertyValues: MutableList<BlockPropertyType.BlockPropertyValue<*, *, *>>) : this(
        identifier,
        HashUtils.computeBlockStateHash(identifier, propertyValues),
        propertyValues
    )

    override fun blockStateHash(): Int {
        return this.blockHash
    }

    override fun unsignedBlockStateHash(): Long {
        return Integer.toUnsignedLong(this.blockHash)
    }

    override fun specialValue(): Short {
        return inSpecialValue
    }

    override fun <DATATYPE, PROPERTY : BlockPropertyType<DATATYPE>> getPropertyValue(p: PROPERTY): DATATYPE {
        for (property in this.blockPropertyValues) {
            if (property.propertyType === p) {
                @Suppress("UNCHECKED_CAST")
                return property.value as DATATYPE
            }
        }
        throw IllegalArgumentException("Property " + p + " is not supported by this block " + this.identifier)
    }

    override fun <DATATYPE, PROPERTY : BlockPropertyType<DATATYPE>> setPropertyValue(
        properties: BlockProperties,
        property: PROPERTY,
        value: DATATYPE
    ): BlockState {
        return setPropertyValue(properties, property.createValue(value))
    }

    override fun setPropertyValue(
        properties: BlockProperties,
        propertyValue: BlockPropertyType.BlockPropertyValue<*, *, *>
    ): BlockState {
        var succeed = false

        val newPropertyValues = blockPropertyValues.map {
            if (it.propertyType === propertyValue.propertyType) {
                succeed = true
                propertyValue
            } else it
        }.toMutableList()

        require(succeed) { "Property " + propertyValue.propertyType + " is not supported by this block " + this.identifier }
        return getNewBlockState(properties, newPropertyValues)!!
    }

    override fun setPropertyValues(
        properties: BlockProperties,
        vararg values: BlockPropertyType.BlockPropertyValue<*, *, *>
    ): BlockState {
        val succeed = BooleanArray(values.size)
        var succeedCount = 0

        val newPropertyValues = blockPropertyValues.map { value ->
            for (j in values.indices) {
                if (values[j].propertyType === value.propertyType) {
                    succeedCount++
                    succeed[j] = true
                    return@map values[j]
                }
            }
            value
        }.toMutableList()

        if (succeedCount != values.size) {
            val errorMsgBuilder = StringBuilder("Properties ")
            for (i in values.indices) {
                if (!succeed[i]) {
                    errorMsgBuilder.append(values[i].propertyType.name)
                    if (i != values.size - 1) errorMsgBuilder.append(", ")
                }
            }
            errorMsgBuilder.append(" are not supported by this block ").append(this.identifier)
            throw IllegalArgumentException(errorMsgBuilder.toString())
        }
        return getNewBlockState(properties, newPropertyValues)!!
    }

    private fun getNewBlockState(
        properties: BlockProperties,
        values: MutableList<BlockPropertyType.BlockPropertyValue<*, *, *>>
    ): BlockState? {
        val bits = properties.specialValueBits
        if (bits <= 16) {
            return properties.getBlockState(BlockState.computeSpecialValue(bits, values))
        } else {
            throw IllegalArgumentException()
        }
    }

    override fun toString(): String {
        return "BlockStateImpl{" +
                "identifier='" + identifier + '\'' +
                ", blockPropertyValues=" + blockPropertyValues.stream().map { it.value }.toList() +
                '}'
    }

    companion object {
        private var UNKNOWN_BLOCK_STATE_CACHE: MutableMap<Int, BlockStateImpl> = HashMap()

        fun makeUnknownBlockState(hash: Int, blockTag: CompoundTag): BlockStateImpl {
            return UNKNOWN_BLOCK_STATE_CACHE.computeIfAbsent(
                hash
            ) {
                BlockStateImpl(
                    BlockID.UNKNOWN,
                    -2,
                    0.toShort(),
                    mutableListOf(),
                    CompoundTagView(
                        LinkedCompoundTag()
                            .putString("name", BlockID.UNKNOWN)
                            .putCompound("states", CompoundTag())
                            .putCompound("Block", blockTag)
                            .putInt("version", ProtocolInfo.BLOCK_STATE_VERSION)
                    )
                )
            }
        }

        private fun buildBlockStateTag(
            identifier: String,
            propertyValues: MutableList<BlockPropertyType.BlockPropertyValue<*, *, *>>
        ): CompoundTagView {
            val states = TreeMapCompoundTag()
            for (value in propertyValues) {
                when (value.propertyType.type) {
                    BlockPropertyType.Type.INT -> states.putInt(
                        value.propertyType.name,
                        value.getSerializedValue() as Int
                    )

                    BlockPropertyType.Type.ENUM -> states.putString(
                        value.propertyType.name,
                        value.getSerializedValue().toString()
                    )

                    BlockPropertyType.Type.BOOLEAN -> states.putByte(
                        value.propertyType.name,
                        (value.getSerializedValue() as Byte).toInt()
                    )
                }
            }
            return CompoundTagView(
                LinkedCompoundTag()
                    .putString("name", identifier)
                    .putCompound("states", states)
                    .putInt("version", ProtocolInfo.BLOCK_STATE_VERSION)
            )
        }
    }
}
