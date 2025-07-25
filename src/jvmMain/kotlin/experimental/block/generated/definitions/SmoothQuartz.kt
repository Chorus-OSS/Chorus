package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object SmoothQuartz : BlockDefinition(
    identifier = "minecraft:smooth_quartz",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(MapColorComponent(r = 255, g = 252, b = 245, a = 255), MineableComponent(hardness = 0.8f))
)
