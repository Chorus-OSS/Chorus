package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object Podzol : BlockDefinition(
    identifier = "minecraft:podzol",
    components = listOf(MapColorComponent(r = 129, g = 86, b = 49, a = 255), MineableComponent(hardness = 0.6f))
)
