package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeElementData
import org.chorus_oss.protocol.types.biome.BiomeSurfaceMaterialAdjustmentData

operator fun BiomeSurfaceMaterialAdjustmentData.Companion.invoke(nbt: CompoundTag): BiomeSurfaceMaterialAdjustmentData {
    return BiomeSurfaceMaterialAdjustmentData(
        adjustment = nbt.getList("adjustments", CompoundTag::class.java).all.map { BiomeElementData.invoke(it) }
    )
}