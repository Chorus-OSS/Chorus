package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object LapisBlock : BlockDefinition(
    identifier = "minecraft:lapis_block",
    components = listOf(MapColorComponent(r = 74, g = 128, b = 255, a = 255), MineableComponent(hardness = 3.0f))
)
