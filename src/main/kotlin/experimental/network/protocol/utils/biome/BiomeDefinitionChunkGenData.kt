package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.*

operator fun BiomeDefinitionChunkGenData.Companion.invoke(nbt: CompoundTag): BiomeDefinitionChunkGenData {
    return BiomeDefinitionChunkGenData(
        climate = if (nbt.containsCompound("climate")) BiomeClimateData.invoke(nbt.getCompound("climate")) else null,
        consolidatedFeatures = if (nbt.containsCompound("consolidatedFeatures")) BiomeConsolidatedFeaturesData.invoke(
            nbt.getCompound("consolidatedFeatures")
        ) else null,
        mountainParams = if (nbt.containsCompound("mountainParams")) BiomeMountainsParamData.invoke(nbt.getCompound("mountainParams")) else null,
        surfaceMaterialAdjustments = if (nbt.containsCompound("surfaceMaterialAdjustments")) BiomeSurfaceMaterialAdjustmentData.invoke(
            nbt.getCompound("surfaceMaterialAdjustments")
        ) else null,
        surfaceMaterials = if (nbt.containsCompound("surfaceMaterials")) BiomeSurfaceMaterialData.invoke(
            nbt.getCompound(
                "surfaceMaterials"
            )
        ) else null,
        hasSwampSurface = nbt.getBoolean("hasSwampSurface"),
        hasFrozenOceanSurface = nbt.getBoolean("hasFrozenOceanSurface"),
        hasTheEndSurface = nbt.getBoolean("hasTheEndSurface"),
        mesaSurface = if (nbt.containsCompound("mesaSurface")) BiomeMesaSurfaceData.invoke(nbt.getCompound("mesaSurface")) else null,
        cappedSurface = if (nbt.containsCompound("cappedSurface")) BiomeCappedSurfaceData.invoke(nbt.getCompound("cappedSurface")) else null,
        overworldGenRules = if (nbt.containsCompound("overworldGenRules")) BiomeOverworldGenRulesData.invoke(
            nbt.getCompound(
                "overworldGenRules"
            )
        ) else null,
        multinoiseGenRules = if (nbt.containsCompound("multinoiseGenRules")) BiomeMultinoiseGenRulesData.invoke(
            nbt.getCompound(
                "multinoiseGenRules"
            )
        ) else null,
        legacyWorldGenRules = if (nbt.containsCompound("legacyWorldGenRules")) BiomeLegacyWorldGenRulesData.invoke(
            nbt.getCompound(
                "legacyWorldGenRules"
            )
        ) else null,
    )
}