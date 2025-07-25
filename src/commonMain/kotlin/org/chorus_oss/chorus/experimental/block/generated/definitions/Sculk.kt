package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object Sculk : BlockDefinition(
    identifier = "minecraft:sculk",
    components = listOf(MapColorComponent(r = 13, g = 18, b = 23, a = 255), MineableComponent(hardness = 0.2f))
)
