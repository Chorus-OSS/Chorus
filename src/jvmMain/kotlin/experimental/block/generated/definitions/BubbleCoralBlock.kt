package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object BubbleCoralBlock : BlockDefinition(
    identifier = "minecraft:bubble_coral_block",
    components = listOf(MapColorComponent(r = 153, g = 90, b = 205, a = 255), MineableComponent(hardness = 1.5f))
)
