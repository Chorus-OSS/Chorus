package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ShortTag
import org.chorus_oss.protocol.types.biome.BiomeTagsData

operator fun BiomeTagsData.Companion.invoke(nbt: CompoundTag): BiomeTagsData {
    return BiomeTagsData(
        tags = nbt.getList("tags", ShortTag::class.java).all.map { it.data }
    )
}