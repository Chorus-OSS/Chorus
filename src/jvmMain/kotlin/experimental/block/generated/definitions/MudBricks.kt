package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object MudBricks : BlockDefinition(
    identifier = "minecraft:mud_bricks",
    components = listOf(MapColorComponent(r = 135, g = 107, b = 98, a = 255), MineableComponent(hardness = 3.0f))
)
