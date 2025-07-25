package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeSurfaceMaterialData

operator fun BiomeSurfaceMaterialData.Companion.invoke(nbt: CompoundTag): BiomeSurfaceMaterialData {
    return BiomeSurfaceMaterialData(
        topBlock = nbt.getInt("topBlock"),
        midBlock = nbt.getInt("midBlock"),
        seaFloorBlock = nbt.getInt("seaFloorBlock"),
        foundationBlock = nbt.getInt("foundationBlock"),
        seaBlock = nbt.getInt("seaBlock"),
        seaFloorDepth = nbt.getInt("seaFloorDepth"),
    )
}