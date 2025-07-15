package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeConditionalTransformationData
import org.chorus_oss.protocol.types.biome.BiomeLegacyWorldGenRulesData

operator fun BiomeLegacyWorldGenRulesData.Companion.invoke(nbt: CompoundTag): BiomeLegacyWorldGenRulesData {
    return BiomeLegacyWorldGenRulesData(
        legacyPreHills = nbt.getList(
            "legacyPreHillsEdge",
            CompoundTag::class.java
        ).all.map { BiomeConditionalTransformationData.invoke(it) }
    )
}