package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object BoneBlock : BlockDefinition(
    identifier = "minecraft:bone_block",
    states = listOf(CommonStates.deprecated, CommonStates.pillarAxis),
    components = listOf(MapColorComponent(r = 247, g = 233, b = 163, a = 255), MineableComponent(hardness = 2.0f))
)
