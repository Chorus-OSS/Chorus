package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object YellowGlazedTerracotta : BlockDefinition(
    identifier = "minecraft:yellow_glazed_terracotta",
    states = listOf(CommonStates.facingDirection),
    components = listOf(
        MapColorComponent(r = 229, g = 229, b = 51, a = 255),
        MineableComponent(hardness = 1.4f),
        MoveableComponent(movement = MoveableComponent.Movement.Both, sticky = false)
    )
)
