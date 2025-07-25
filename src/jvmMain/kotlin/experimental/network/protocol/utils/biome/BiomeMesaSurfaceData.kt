package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeMesaSurfaceData

operator fun BiomeMesaSurfaceData.Companion.invoke(nbt: CompoundTag): BiomeMesaSurfaceData {
    return BiomeMesaSurfaceData(
        clayMaterial = nbt.getInt("clayMaterial").toUInt(),
        hardClayMaterial = nbt.getInt("hardClayMaterial").toUInt(),
        brycePillars = nbt.getBoolean("brycePillars"),
        hasForest = nbt.getBoolean("hasForest"),
    )
}