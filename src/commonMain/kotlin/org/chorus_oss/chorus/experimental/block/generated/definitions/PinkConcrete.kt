package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object PinkConcrete : BlockDefinition(
    identifier = "minecraft:pink_concrete",
    components = listOf(MapColorComponent(r = 242, g = 127, b = 165, a = 255), MineableComponent(hardness = 1.8f))
)
