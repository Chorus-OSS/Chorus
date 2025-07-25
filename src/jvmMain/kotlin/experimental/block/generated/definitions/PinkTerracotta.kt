package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object PinkTerracotta : BlockDefinition(
    identifier = "minecraft:pink_terracotta",
    components = listOf(MapColorComponent(r = 160, g = 77, b = 78, a = 255), MineableComponent(hardness = 1.25f))
)
