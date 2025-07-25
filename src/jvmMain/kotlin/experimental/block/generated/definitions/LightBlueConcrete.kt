package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object LightBlueConcrete : BlockDefinition(
    identifier = "minecraft:light_blue_concrete",
    components = listOf(MapColorComponent(r = 102, g = 153, b = 216, a = 255), MineableComponent(hardness = 1.8f))
)
