package org.chorus_oss.chorus.level.format.palette

import org.chorus_oss.chorus.nbt.tag.CompoundTag

fun interface PersistentDataSerializer<V> {
    fun serialize(value: V): CompoundTag
}
