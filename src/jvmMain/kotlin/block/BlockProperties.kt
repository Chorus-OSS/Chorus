package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.type.BlockPropertyType
import org.chorus_oss.chorus.block.property.type.BlockPropertyType.BlockPropertyValue
import org.chorus_oss.chorus.experimental.utils.BlockStates
import org.chorus_oss.chorus.tags.BlockTags.register
import org.chorus_oss.chorus.utils.Identifier.Companion.assertValid

class BlockProperties(identifier: String, blockTags: Set<String>, vararg properties: BlockPropertyType<*>) {
    val identifier: String
    private val propertyTypeSet: Set<BlockPropertyType<*>>
    val specialValueMap: Map<Short, BlockState>
    val defaultState: BlockState
    val specialValueBits: Byte

    val allStates: List<BlockState>

    constructor(identifier: String, vararg properties: BlockPropertyType<*>) : this(
        identifier, setOf<String>(), *properties
    )

    init {
        assertValid(identifier)
        register(identifier, blockTags)
        this.identifier = identifier.intern()
        this.propertyTypeSet = mutableSetOf(*properties)

        this.specialValueBits = propertyTypeSet.sumOf { it.bitSize.toInt() }.toByte().also {
            if (it > 16) throw IllegalStateException("State limit exceeded for block: $identifier")
        }

        val propertyList = propertyTypeSet.toList()

        this.allStates = BlockStates.getAllStates(identifier, propertyList)

        this.defaultState = this.allStates.first()
        this.specialValueMap = this.allStates.associateBy { it.specialValue() }
    }

    fun getBlockState(specialValue: Short): BlockState? {
        return specialValueMap[specialValue]
    }

    fun <DATATYPE, PROPERTY : BlockPropertyType<DATATYPE>> getBlockState(
        property: PROPERTY, value: DATATYPE
    ): BlockState {
        return defaultState.setPropertyValue(this, property, value)
    }

    fun getBlockState(propertyValue: BlockPropertyValue<*, *, *>): BlockState {
        return defaultState.setPropertyValue(this, propertyValue)
    }

    fun getBlockState(vararg values: BlockPropertyValue<*, *, *>): BlockState {
        return defaultState.setPropertyValues(this, *values)
    }

    fun containBlockState(blockState: BlockState): Boolean {
        return specialValueMap.containsValue(blockState)
    }

    fun containBlockState(specialValue: Short): Boolean {
        return specialValueMap.containsKey(specialValue)
    }

    fun <DATATYPE, PROPERTY : BlockPropertyType<DATATYPE>> containProperty(property: PROPERTY): Boolean {
        return propertyTypeSet.contains(property)
    }

    fun <DATATYPE, PROPERTY : BlockPropertyType<DATATYPE>> getPropertyValue(specialValue: Int, p: PROPERTY): DATATYPE {
        return getBlockState(specialValue.toShort())?.getPropertyValue(p) ?: p.defaultValue
    }

    fun getPropertyTypeSet(): Set<BlockPropertyType<*>> {
        return propertyTypeSet
    }
}
