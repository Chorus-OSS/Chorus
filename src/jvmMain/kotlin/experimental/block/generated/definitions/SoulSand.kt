package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object SoulSand : BlockDefinition(
    identifier = "minecraft:soul_sand",
    components = listOf(MapColorComponent(r = 102, g = 76, b = 51, a = 255), MineableComponent(hardness = 0.5f))
)
