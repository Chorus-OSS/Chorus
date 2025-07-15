package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.IntTag
import org.chorus_oss.protocol.types.biome.BiomeCappedSurfaceData

operator fun BiomeCappedSurfaceData.Companion.invoke(nbt: CompoundTag): BiomeCappedSurfaceData {
    return BiomeCappedSurfaceData(
        floorBlocks = nbt.getList("floorBlocks", IntTag::class.java).all.map { it.data },
        ceilingBlocks = nbt.getList("ceilingBlocks", IntTag::class.java).all.map { it.data },
        seaBlock = if (nbt.containsInt("seaBlock")) nbt.getInt("seaBlock").toUInt() else null,
        foundationBlock = if (nbt.containsInt("foundationBlock")) nbt.getInt("foundationBlock").toUInt() else null,
        beachBlock = if (nbt.containsInt("beachBlock")) nbt.getInt("beachBlock").toUInt() else null,
    )
}