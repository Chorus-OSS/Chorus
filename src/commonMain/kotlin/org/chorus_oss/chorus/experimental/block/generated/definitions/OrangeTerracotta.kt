package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object OrangeTerracotta : BlockDefinition(
    identifier = "minecraft:orange_terracotta",
    components = listOf(MapColorComponent(r = 159, g = 82, b = 36, a = 255), MineableComponent(hardness = 1.25f))
)
