package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object PurpleConcretePowder : BlockDefinition(
    identifier = "minecraft:purple_concrete_powder",
    components = listOf(MapColorComponent(r = 153, g = 90, b = 205, a = 255), MineableComponent(hardness = 0.5f))
)
