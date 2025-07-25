package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object BrownGlazedTerracotta : BlockDefinition(
    identifier = "minecraft:brown_glazed_terracotta",
    states = listOf(CommonStates.facingDirection),
    components = listOf(
        MapColorComponent(r = 102, g = 76, b = 51, a = 255),
        MineableComponent(hardness = 1.4f),
        MoveableComponent(movement = MoveableComponent.Movement.Both, sticky = false)
    )
)
