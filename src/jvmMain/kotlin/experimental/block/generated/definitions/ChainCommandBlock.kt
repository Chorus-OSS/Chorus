package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object ChainCommandBlock : BlockDefinition(
    identifier = "minecraft:chain_command_block",
    states = listOf(CommonStates.conditionalBit, CommonStates.facingDirection),
    components = listOf(
        MapColorComponent(r = 102, g = 127, b = 51, a = 255),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
