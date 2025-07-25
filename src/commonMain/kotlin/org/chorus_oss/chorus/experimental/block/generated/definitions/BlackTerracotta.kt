package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object BlackTerracotta : BlockDefinition(
    identifier = "minecraft:black_terracotta",
    components = listOf(MapColorComponent(r = 37, g = 22, b = 16, a = 255), MineableComponent(hardness = 1.25f))
)
