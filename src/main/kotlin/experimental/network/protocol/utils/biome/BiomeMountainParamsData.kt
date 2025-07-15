package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeMountainsParamData

operator fun BiomeMountainsParamData.Companion.invoke(nbt: CompoundTag): BiomeMountainsParamData {
    return BiomeMountainsParamData(
        steepBlock = nbt.getInt("steepBlock"),
        northSlopes = nbt.getBoolean("northSlopes"),
        southSlopes = nbt.getBoolean("southSlopes"),
        westSlopes = nbt.getBoolean("westSlopes"),
        eastSlopes = nbt.getBoolean("eastSlopes"),
        topSlideEnabled = nbt.getBoolean("topSlideEnabled"),
    )
}