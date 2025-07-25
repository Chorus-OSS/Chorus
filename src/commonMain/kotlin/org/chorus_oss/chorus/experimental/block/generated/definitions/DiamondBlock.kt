package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object DiamondBlock : BlockDefinition(
    identifier = "minecraft:diamond_block",
    components = listOf(MapColorComponent(r = 92, g = 219, b = 213, a = 255), MineableComponent(hardness = 5.0f))
)
