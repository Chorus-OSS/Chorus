package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object BrainCoralBlock : BlockDefinition(
    identifier = "minecraft:brain_coral_block",
    components = listOf(MapColorComponent(r = 242, g = 127, b = 165, a = 255), MineableComponent(hardness = 1.5f))
)
