package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object TuffBricks : BlockDefinition(
    identifier = "minecraft:tuff_bricks",
    components = listOf(MapColorComponent(r = 57, g = 41, b = 35, a = 255), MineableComponent(hardness = 1.5f))
)
