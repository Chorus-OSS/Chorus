package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeConditionalTransformationData
import org.chorus_oss.protocol.types.biome.BiomeOverworldGenRulesData
import org.chorus_oss.protocol.types.biome.BiomeWeightedData
import org.chorus_oss.protocol.types.biome.BiomeWeightedTemperatureData

operator fun BiomeOverworldGenRulesData.Companion.invoke(nbt: CompoundTag): BiomeOverworldGenRulesData {
    return BiomeOverworldGenRulesData(
        hillsTransformations = nbt.getList(
            "hillsTransformations",
            CompoundTag::class.java
        ).all.map { BiomeWeightedData.invoke(it) },
        mutateTransformations = nbt.getList(
            "mutateTransformations",
            CompoundTag::class.java
        ).all.map { BiomeWeightedData.invoke(it) },
        riverTransformations = nbt.getList(
            "riverTransformations",
            CompoundTag::class.java
        ).all.map { BiomeWeightedData.invoke(it) },
        shoreTransformations = nbt.getList(
            "shoreTransformations",
            CompoundTag::class.java
        ).all.map { BiomeWeightedData.invoke(it) },
        preHillsEdge = nbt.getList(
            "preHillsEdge",
            CompoundTag::class.java
        ).all.map { BiomeConditionalTransformationData.invoke(it) },
        postShoreEdge = nbt.getList(
            "postShoreEdge",
            CompoundTag::class.java
        ).all.map { BiomeConditionalTransformationData.invoke(it) },
        climate = nbt.getList("climate", CompoundTag::class.java).all.map { BiomeWeightedTemperatureData.invoke(it) },
    )
}