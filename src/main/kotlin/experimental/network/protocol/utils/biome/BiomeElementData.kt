package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeElementData
import org.chorus_oss.protocol.types.biome.BiomeSurfaceMaterialData
import org.chorus_oss.protocol.types.biome.ExpressionOp

operator fun BiomeElementData.Companion.invoke(nbt: CompoundTag): BiomeElementData {
    return BiomeElementData(
        noiseFrequencyScale = nbt.getFloat("noiseFreqScale"),
        noiseLowerBound = nbt.getFloat("noiseLowerBound"),
        noiseUpperBound = nbt.getFloat("noiseUpperBound"),
        heightMinType = ExpressionOp.entries[nbt.getInt("heightMinType")],
        heightMin = nbt.getShort("heightMin"),
        heightMaxType = ExpressionOp.entries[nbt.getInt("heightMaxType")],
        heightMax = nbt.getShort("heightMax"),
        adjustedMaterials = BiomeSurfaceMaterialData.invoke(nbt.getCompound("adjustedMaterials"))
    )
}