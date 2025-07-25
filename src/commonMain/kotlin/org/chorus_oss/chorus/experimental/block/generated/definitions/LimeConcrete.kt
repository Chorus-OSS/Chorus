package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object LimeConcrete : BlockDefinition(
    identifier = "minecraft:lime_concrete",
    components = listOf(MapColorComponent(r = 127, g = 204, b = 25, a = 255), MineableComponent(hardness = 1.8f))
)
