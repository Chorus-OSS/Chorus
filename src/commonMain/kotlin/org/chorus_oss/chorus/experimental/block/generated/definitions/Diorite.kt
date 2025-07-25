package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object Diorite : BlockDefinition(
    identifier = "minecraft:diorite",
    components = listOf(MapColorComponent(r = 255, g = 252, b = 245, a = 255), MineableComponent(hardness = 1.5f))
)
