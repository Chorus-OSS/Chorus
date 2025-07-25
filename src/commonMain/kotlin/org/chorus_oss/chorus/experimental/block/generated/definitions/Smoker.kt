package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Smoker : BlockDefinition(
    identifier = "minecraft:smoker",
    states = listOf(CommonStates.minecraftCardinalDirection),
    components = listOf(MapColorComponent(r = 112, g = 112, b = 112, a = 255), MineableComponent(hardness = 3.5f))
)
