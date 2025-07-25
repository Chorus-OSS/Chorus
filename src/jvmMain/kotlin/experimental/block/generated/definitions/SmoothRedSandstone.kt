package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object SmoothRedSandstone : BlockDefinition(
    identifier = "minecraft:smooth_red_sandstone",
    components = listOf(MapColorComponent(r = 216, g = 127, b = 51, a = 255), MineableComponent(hardness = 2.0f))
)
