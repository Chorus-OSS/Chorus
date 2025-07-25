package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object WarpedWartBlock : BlockDefinition(
    identifier = "minecraft:warped_wart_block",
    components = listOf(MapColorComponent(r = 20, g = 180, b = 133, a = 255), MineableComponent(hardness = 1.0f))
)
