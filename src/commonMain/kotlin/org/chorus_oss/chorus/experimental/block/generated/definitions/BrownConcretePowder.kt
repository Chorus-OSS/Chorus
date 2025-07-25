package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object BrownConcretePowder : BlockDefinition(
    identifier = "minecraft:brown_concrete_powder",
    components = listOf(MapColorComponent(r = 102, g = 76, b = 51, a = 255), MineableComponent(hardness = 0.5f))
)
