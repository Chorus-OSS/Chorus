package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object DeadBubbleCoralBlock : BlockDefinition(
    identifier = "minecraft:dead_bubble_coral_block",
    components = listOf(MapColorComponent(r = 76, g = 76, b = 76, a = 255), MineableComponent(hardness = 1.5f))
)
