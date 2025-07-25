package org.chorus_oss.chorus.entity.data.property

import org.chorus_oss.chorus.nbt.tag.CompoundTag

class FloatEntityProperty(
    identifier: String,
    private val defaultValue: Float,
    private val maxValue: Float,
    private val minValue: Float
) :
    EntityProperty(identifier) {
    fun getDefaultValue(): Float {
        return defaultValue
    }

    fun getMaxValue(): Float {
        return maxValue
    }

    fun getMinValue(): Float {
        return minValue
    }

    override fun populateTag(tag: CompoundTag) {
        tag.putInt("type", 1)
        tag.putFloat("max", getMaxValue())
        tag.putFloat("min", getMinValue())
    }
}