package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeConditionalTransformationData
import org.chorus_oss.protocol.types.biome.BiomeWeightedData

operator fun BiomeConditionalTransformationData.Companion.invoke(nbt: CompoundTag): BiomeConditionalTransformationData {
    return BiomeConditionalTransformationData(
        weightedBiome = nbt.getList("transformsInto", CompoundTag::class.java).all.map { BiomeWeightedData.invoke(it) },
        conditionJSON = nbt.getShort("conditionJSON"),
        minPassingNeighbors = nbt.getInt("minPassingNeighbors").toUInt()
    )
}