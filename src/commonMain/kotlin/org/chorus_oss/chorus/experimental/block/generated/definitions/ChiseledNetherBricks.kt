package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object ChiseledNetherBricks : BlockDefinition(
    identifier = "minecraft:chiseled_nether_bricks",
    components = listOf(MapColorComponent(r = 112, g = 2, b = 0, a = 255), MineableComponent(hardness = 2.0f))
)
