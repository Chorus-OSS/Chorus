package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object Azalea : BlockDefinition(
    identifier = "minecraft:azalea",
    components = listOf(MapColorComponent(r = 0, g = 124, b = 0, a = 255), MineableComponent(hardness = 0.0f))
)
