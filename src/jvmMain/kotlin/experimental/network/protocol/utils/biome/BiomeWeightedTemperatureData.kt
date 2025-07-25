package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeTemperatureCategory
import org.chorus_oss.protocol.types.biome.BiomeWeightedTemperatureData

operator fun BiomeWeightedTemperatureData.Companion.invoke(nbt: CompoundTag): BiomeWeightedTemperatureData {
    return BiomeWeightedTemperatureData(
        temperature = BiomeTemperatureCategory.entries[nbt.getInt("temperature")],
        weight = nbt.getInt("weight").toUInt()
    )
}