package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object WhiteGlazedTerracotta : BlockDefinition(
    identifier = "minecraft:white_glazed_terracotta",
    states = listOf(CommonStates.facingDirection),
    components = listOf(
        MapColorComponent(r = 255, g = 255, b = 255, a = 255),
        MineableComponent(hardness = 1.4f),
        MoveableComponent(movement = MoveableComponent.Movement.Both, sticky = false)
    )
)
