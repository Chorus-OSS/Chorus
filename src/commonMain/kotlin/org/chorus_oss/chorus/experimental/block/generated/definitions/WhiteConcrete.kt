package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object WhiteConcrete : BlockDefinition(
    identifier = "minecraft:white_concrete",
    components = listOf(MapColorComponent(r = 255, g = 255, b = 255, a = 255), MineableComponent(hardness = 1.8f))
)
