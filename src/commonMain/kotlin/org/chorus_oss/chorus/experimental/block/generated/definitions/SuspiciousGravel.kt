package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object SuspiciousGravel : BlockDefinition(
    identifier = "minecraft:suspicious_gravel",
    states = listOf(CommonStates.brushedProgress, CommonStates.hanging),
    components = listOf(MapColorComponent(r = 112, g = 112, b = 112, a = 255), MineableComponent(hardness = 0.25f))
)
