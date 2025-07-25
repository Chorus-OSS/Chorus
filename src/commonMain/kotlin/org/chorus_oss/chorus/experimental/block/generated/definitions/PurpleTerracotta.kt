package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object PurpleTerracotta : BlockDefinition(
    identifier = "minecraft:purple_terracotta",
    components = listOf(MapColorComponent(r = 122, g = 73, b = 88, a = 255), MineableComponent(hardness = 1.25f))
)
