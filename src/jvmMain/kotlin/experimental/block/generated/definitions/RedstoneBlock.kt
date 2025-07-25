package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object RedstoneBlock : BlockDefinition(
    identifier = "minecraft:redstone_block",
    components = listOf(MapColorComponent(r = 255, g = 0, b = 0, a = 255), MineableComponent(hardness = 5.0f))
)
