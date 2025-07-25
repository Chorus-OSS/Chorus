package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object BlueTerracotta : BlockDefinition(
    identifier = "minecraft:blue_terracotta",
    components = listOf(MapColorComponent(r = 76, g = 62, b = 92, a = 255), MineableComponent(hardness = 1.25f))
)
