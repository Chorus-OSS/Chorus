package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object ChiseledResinBricks : BlockDefinition(
    identifier = "minecraft:chiseled_resin_bricks",
    components = listOf(MapColorComponent(r = 159, g = 82, b = 36, a = 255), MineableComponent(hardness = 1.5f))
)
