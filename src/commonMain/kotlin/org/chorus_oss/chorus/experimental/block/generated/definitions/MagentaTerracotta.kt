package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object MagentaTerracotta : BlockDefinition(
    identifier = "minecraft:magenta_terracotta",
    components = listOf(MapColorComponent(r = 149, g = 87, b = 108, a = 255), MineableComponent(hardness = 1.25f))
)
