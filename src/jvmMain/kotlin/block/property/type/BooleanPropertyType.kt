package org.chorus_oss.chorus.block.property.type

import org.chorus_oss.chorus.block.property.type.BlockPropertyType.BlockPropertyValue

class BooleanPropertyType private constructor(name: String, defaultData: Boolean) :
    BaseBlockPropertyType<Boolean>(name, listOf(true, false), defaultData, 1.toByte()) {
    private val FALSE = BooleanPropertyValue(false)
    private val TRUE = BooleanPropertyValue(true)

    override val type: BlockPropertyType.Type
        get() = BlockPropertyType.Type.BOOLEAN

    override fun createValue(value: Boolean): BooleanPropertyValue {
        return if (value) TRUE else FALSE
    }

    override fun tryCreateValue(value: Any?): BooleanPropertyValue {
        if (value is Boolean) {
            return if (value) TRUE else FALSE
        } else if (value is Number) {
            val intValue = value.toInt()
            if (intValue == 0 || intValue == 1) return if (intValue == 1) TRUE else FALSE
        }
        throw IllegalArgumentException("Invalid value for boolean property type: $value")
    }

    inner class BooleanPropertyValue internal constructor(value: Boolean) :
        BlockPropertyValue<Boolean, BooleanPropertyType, Byte?>(this@BooleanPropertyType, value) {
        override fun getIndex(): Int = if (value) 1 else 0

        override fun getSerializedValue(): Byte = (if (value) 1 else 0).toByte()

        override fun toString(): String {
            return "BoolPropertyValue(name=${name}, value=${value})"
        }
    }

    companion object {
        fun of(name: String, defaultData: Boolean): BooleanPropertyType {
            return BooleanPropertyType(name, defaultData)
        }
    }
}
