package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeWeightedData

operator fun BiomeWeightedData.Companion.invoke(nbt: CompoundTag): BiomeWeightedData {
    return BiomeWeightedData(
        biome = nbt.getShort("biomeIdentifier"),
        weight = nbt.getInt("weight").toUInt()
    )
}