package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object PointedDripstone : BlockDefinition(
    identifier = "minecraft:pointed_dripstone",
    states = listOf(CommonStates.dripstoneThickness, CommonStates.hanging),
    components = listOf(
        MineableComponent(hardness = 1.5f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
