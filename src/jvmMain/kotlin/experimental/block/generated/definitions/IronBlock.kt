package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object IronBlock : BlockDefinition(
    identifier = "minecraft:iron_block",
    components = listOf(MapColorComponent(r = 167, g = 167, b = 167, a = 255), MineableComponent(hardness = 5.0f))
)
