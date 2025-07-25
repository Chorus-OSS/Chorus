package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeClimateData

operator fun BiomeClimateData.Companion.invoke(nbt: CompoundTag): BiomeClimateData {
    return BiomeClimateData(
        temperature = nbt.getFloat("temperature"),
        downfall = nbt.getFloat("downfall"),
        redSporeDensity = nbt.getFloat("redSporeDensity"),
        blueSporeDensity = nbt.getFloat("blueSporeDensity"),
        ashDensity = nbt.getFloat("ashDensity"),
        whiteAshDensity = nbt.getFloat("whiteAshDensity"),
        snowAccumulationMin = nbt.getFloat("snowAccumulationMin"),
        snowAccumulationMax = nbt.getFloat("snowAccumulationMax"),
    )
}