package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object Clay : BlockDefinition(
    identifier = "minecraft:clay",
    components = listOf(MapColorComponent(r = 164, g = 168, b = 184, a = 255), MineableComponent(hardness = 0.6f))
)
