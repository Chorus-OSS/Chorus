package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object WhiteTerracotta : BlockDefinition(
    identifier = "minecraft:white_terracotta",
    components = listOf(MapColorComponent(r = 209, g = 177, b = 161, a = 255), MineableComponent(hardness = 1.25f))
)
