package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object GreenTerracotta : BlockDefinition(
    identifier = "minecraft:green_terracotta",
    components = listOf(MapColorComponent(r = 76, g = 82, b = 42, a = 255), MineableComponent(hardness = 1.25f))
)
