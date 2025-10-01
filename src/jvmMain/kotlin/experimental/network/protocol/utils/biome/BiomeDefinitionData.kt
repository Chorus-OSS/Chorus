package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeDefinitionChunkGenData
import org.chorus_oss.protocol.types.biome.BiomeDefinitionData
import org.chorus_oss.protocol.types.biome.BiomeTagsData

operator fun BiomeDefinitionData.Companion.invoke(nbt: CompoundTag): BiomeDefinitionData {
    return BiomeDefinitionData(
        id = (-1).toUShort(),
        temperature = nbt.getFloat("temperature"),
        downfall = nbt.getFloat("downfall"),
        foliageSnow = 0f,
        depth = nbt.getFloat("depth"),
        scale = nbt.getFloat("scale"),
        mapWaterColorARGB = nbt.getInt("mapWaterColorARGB"),
        rain = nbt.getBoolean("rain"),
        tags = if (nbt.containsCompound("tags")) BiomeTagsData.invoke(nbt.getCompound("tags")) else null,
        chunkGenData = if (nbt.containsCompound("chunkGenData")) BiomeDefinitionChunkGenData.invoke(nbt.getCompound("chunkGenData")) else null,
    )
}