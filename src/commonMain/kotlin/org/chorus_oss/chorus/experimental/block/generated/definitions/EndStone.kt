package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object EndStone : BlockDefinition(
    identifier = "minecraft:end_stone",
    components = listOf(MapColorComponent(r = 247, g = 233, b = 163, a = 255), MineableComponent(hardness = 3.0f))
)
