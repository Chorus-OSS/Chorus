package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object CrackedStoneBricks : BlockDefinition(
    identifier = "minecraft:cracked_stone_bricks",
    components = listOf(MapColorComponent(r = 112, g = 112, b = 112, a = 255), MineableComponent(hardness = 1.5f))
)
