package org.chorus_oss.chorus.block.property.type

import org.chorus_oss.chorus.block.property.type.BlockPropertyType.BlockPropertyValue
import org.chorus_oss.chorus.utils.Utils
import java.util.*

class EnumPropertyType<T : Enum<T>>(name: String, val enumClass: Class<T>, defaultData: T) : BaseBlockPropertyType<T>(
    name,
    enumClass.enumConstants.toList(),
    defaultData,
    Utils.computeRequiredBits(0, enumClass.enumConstants.size - 1)
) {
    val cachedValues: EnumMap<T, EnumPropertyValue> = EnumMap(validValues.associateWith { EnumPropertyValue(it) })

    override val type: BlockPropertyType.Type
        get() = BlockPropertyType.Type.ENUM

    override fun createValue(value: T): EnumPropertyValue {
        return cachedValues[value]!!
    }

    override fun tryCreateValue(value: Any?): EnumPropertyValue? {
        if (enumClass.isInstance(value)) {
            return cachedValues[enumClass.cast(value)]
        } else if (value is String) {
            return cachedValues[enumClass.enumConstants.find { it.name.equals(value, ignoreCase = true) }]
        }
        throw IllegalArgumentException("Invalid value for enum property type: $value")
    }

    inner class EnumPropertyValue internal constructor(value: T) :
        BlockPropertyValue<T, EnumPropertyType<T>, String?>(this@EnumPropertyType, value) {
        private val serializedValue: String = value.name.lowercase()

        override fun getIndex(): Int {
            return value.ordinal
        }

        override fun getSerializedValue(): String {
            return serializedValue
        }

        override fun toString(): String {
            return "EnumPropertyValue(name=$name, value=$value)"
        }
    }

    companion object {
        fun <T : Enum<T>> of(name: String, enumClass: Class<T>, defaultData: T): EnumPropertyType<T> {
            return EnumPropertyType(name, enumClass, defaultData)
        }
    }
}