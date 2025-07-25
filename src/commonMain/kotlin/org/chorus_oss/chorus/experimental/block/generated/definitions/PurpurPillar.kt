package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object PurpurPillar : BlockDefinition(
    identifier = "minecraft:purpur_pillar",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(MapColorComponent(r = 178, g = 76, b = 216, a = 255), MineableComponent(hardness = 1.5f))
)
