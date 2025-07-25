package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object LightBlueTerracotta : BlockDefinition(
    identifier = "minecraft:light_blue_terracotta",
    components = listOf(MapColorComponent(r = 112, g = 108, b = 138, a = 255), MineableComponent(hardness = 1.25f))
)
