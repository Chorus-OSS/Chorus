package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object BlueConcretePowder : BlockDefinition(
    identifier = "minecraft:blue_concrete_powder",
    components = listOf(MapColorComponent(r = 51, g = 76, b = 178, a = 255), MineableComponent(hardness = 0.5f))
)
