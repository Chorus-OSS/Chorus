package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object InfestedDeepslate : BlockDefinition(
    identifier = "minecraft:infested_deepslate",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(MapColorComponent(r = 100, g = 100, b = 100, a = 255), MineableComponent(hardness = 1.5f))
)
