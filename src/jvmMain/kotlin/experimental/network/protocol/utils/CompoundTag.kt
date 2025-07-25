package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.nbt.Tag
import org.chorus_oss.nbt.tags.CompoundTag

operator fun CompoundTag.Companion.invoke(tag: org.chorus_oss.chorus.nbt.tag.CompoundTag): CompoundTag {
    return Tag.invoke(tag) as CompoundTag
}