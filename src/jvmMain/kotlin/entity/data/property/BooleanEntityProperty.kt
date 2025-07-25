package org.chorus_oss.chorus.entity.data.property

import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BooleanEntityProperty(identifier: String, private val defaultValue: Boolean) : EntityProperty(identifier) {
    fun getDefaultValue(): Boolean {
        return defaultValue
    }

    override fun populateTag(tag: CompoundTag) {
        tag.putInt("type", 2)
    }
}
