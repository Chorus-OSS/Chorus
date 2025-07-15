package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeMultinoiseGenRulesData

operator fun BiomeMultinoiseGenRulesData.Companion.invoke(nbt: CompoundTag): BiomeMultinoiseGenRulesData {
    return BiomeMultinoiseGenRulesData(
        temperature = nbt.getFloat("temperature"),
        humidity = nbt.getFloat("humidity"),
        altitude = nbt.getFloat("altitude"),
        weirdness = nbt.getFloat("weirdness"),
        weight = nbt.getFloat("weight"),
    )
}