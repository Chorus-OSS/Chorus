package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object CrimsonNylium : BlockDefinition(
    identifier = "minecraft:crimson_nylium",
    components = listOf(MapColorComponent(r = 189, g = 48, b = 49, a = 255), MineableComponent(hardness = 0.4f))
)
