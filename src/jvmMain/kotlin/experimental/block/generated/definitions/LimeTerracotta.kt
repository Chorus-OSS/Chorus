package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object LimeTerracotta : BlockDefinition(
    identifier = "minecraft:lime_terracotta",
    components = listOf(MapColorComponent(r = 103, g = 117, b = 53, a = 255), MineableComponent(hardness = 1.25f))
)
