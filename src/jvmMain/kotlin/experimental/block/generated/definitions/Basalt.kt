package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Basalt : BlockDefinition(
    identifier = "minecraft:basalt",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(MapColorComponent(r = 25, g = 25, b = 25, a = 255), MineableComponent(hardness = 1.25f))
)
