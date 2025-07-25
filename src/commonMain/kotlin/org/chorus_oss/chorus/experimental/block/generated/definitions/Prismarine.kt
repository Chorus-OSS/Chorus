package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object Prismarine : BlockDefinition(
    identifier = "minecraft:prismarine",
    components = listOf(MapColorComponent(r = 76, g = 127, b = 153, a = 255), MineableComponent(hardness = 1.5f))
)
