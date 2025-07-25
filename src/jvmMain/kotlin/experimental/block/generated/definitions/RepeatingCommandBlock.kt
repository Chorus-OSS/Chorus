package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object RepeatingCommandBlock : BlockDefinition(
    identifier = "minecraft:repeating_command_block",
    states = listOf(CommonStates.conditionalBit, CommonStates.facingDirection),
    components = listOf(
        MapColorComponent(r = 153, g = 90, b = 205, a = 255),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
