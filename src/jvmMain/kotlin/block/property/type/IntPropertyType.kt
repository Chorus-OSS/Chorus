package org.chorus_oss.chorus.block.property.type

import org.chorus_oss.chorus.block.property.type.BlockPropertyType.BlockPropertyValue
import org.chorus_oss.chorus.utils.Utils.computeRequiredBits

import java.util.stream.IntStream

class IntPropertyType private constructor(
    name: String,
    val min: Int,
    val max: Int,
    defaultData: Int
) :
    BaseBlockPropertyType<Int>(
        name, IntStream.range(min, max + 1).boxed().toList(), defaultData, computeRequiredBits(
            min,
            max
        )
    ) {
    private val cachedValues: Array<IntPropertyValue> = Array(max - min + 1) { i -> IntPropertyValue(i + min) }

    override val type: BlockPropertyType.Type
        get() {
            return BlockPropertyType.Type.INT
        }

    override fun createValue(value: Int): IntPropertyValue {
        return cachedValues[value - min]
    }

    override fun tryCreateValue(value: Any?): IntPropertyValue {
        if (value is Number) {
            return cachedValues[value.toInt() - min]
        } else throw IllegalArgumentException("Invalid value for int property type: $value")
    }

    inner class IntPropertyValue internal constructor(value: Int) :
        BlockPropertyValue<Int, IntPropertyType, Int?>(this@IntPropertyType, value) {
        override fun getIndex(): Int {
            return value - min
        }

        override fun getSerializedValue(): Int {
            return value
        }

        override fun toString(): String {
            return "IntPropertyValue(name=${name}, value=${value})"
        }
    }

    companion object {
        fun of(name: String, min: Int, max: Int, defaultData: Int): IntPropertyType {
            return IntPropertyType(name, min, max, defaultData)
        }
    }
}
