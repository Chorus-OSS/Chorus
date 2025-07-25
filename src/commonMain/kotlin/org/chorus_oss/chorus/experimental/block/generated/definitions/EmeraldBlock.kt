package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object EmeraldBlock : BlockDefinition(
    identifier = "minecraft:emerald_block",
    components = listOf(MapColorComponent(r = 0, g = 217, b = 58, a = 255), MineableComponent(hardness = 5.0f))
)
