package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.nbt.Tag
import org.chorus_oss.nbt.tags.*

operator fun Tag.Companion.invoke(tag: org.chorus_oss.chorus.nbt.tag.Tag<*>): Tag {
    return when (tag) {
        is org.chorus_oss.chorus.nbt.tag.ByteArrayTag -> ByteArrayTag(tag.data.toList())
        is org.chorus_oss.chorus.nbt.tag.ByteTag -> ByteTag(tag.data.toByte())
        is org.chorus_oss.chorus.nbt.tag.CompoundTag -> CompoundTag(tag.tags.mapValues { Tag.invoke(it.value) })
        is org.chorus_oss.chorus.nbt.tag.DoubleTag -> DoubleTag(tag.data)
        is org.chorus_oss.chorus.nbt.tag.EndTag -> EndTag()
        is org.chorus_oss.chorus.nbt.tag.FloatTag -> FloatTag(tag.data)
        is org.chorus_oss.chorus.nbt.tag.IntArrayTag -> IntArrayTag(tag.data.toList())
        is org.chorus_oss.chorus.nbt.tag.IntTag -> IntTag(tag.data)
        is org.chorus_oss.chorus.nbt.tag.ListTag<*> -> ListTag(tag.all.map { Tag.invoke(it) })
        is org.chorus_oss.chorus.nbt.tag.LongTag -> LongTag(tag.data)
        is org.chorus_oss.chorus.nbt.tag.ShortTag -> ShortTag(tag.data)
        is org.chorus_oss.chorus.nbt.tag.StringTag -> StringTag(tag.data)
        else -> throw NotImplementedError("Unsupported tag type")
    }
}