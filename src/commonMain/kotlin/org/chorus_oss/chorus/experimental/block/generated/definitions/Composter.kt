package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Composter : BlockDefinition(
    identifier = "minecraft:composter",
    states = listOf(CommonStates.composterFillLevel),
    components = listOf(MapColorComponent(r = 143, g = 119, b = 72, a = 255), MineableComponent(hardness = 0.6f))
)
