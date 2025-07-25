package org.chorus_oss.chorus.entity.data.property

import org.chorus_oss.chorus.nbt.tag.CompoundTag

class IntEntityProperty(
    identifier: String,
    private val defaultValue: Int,
    private val maxValue: Int,
    private val minValue: Int
) :
    EntityProperty(identifier) {
    fun getDefaultValue(): Int {
        return defaultValue
    }

    fun getMaxValue(): Int {
        return maxValue
    }

    fun getMinValue(): Int {
        return minValue
    }

    override fun populateTag(tag: CompoundTag) {
        tag.putInt("type", 0)
        tag.putInt("max", getMaxValue())
        tag.putInt("min", getMinValue())
    }
}
