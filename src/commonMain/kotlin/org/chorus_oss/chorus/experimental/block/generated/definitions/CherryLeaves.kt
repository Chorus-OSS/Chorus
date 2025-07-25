package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object CherryLeaves : BlockDefinition(
    identifier = "minecraft:cherry_leaves",
    states = listOf(CommonStates.persistentBit, CommonStates.updateBit),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 242, g = 127, b = 165, a = 255),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 30, destroyChance = 60),
        MineableComponent(hardness = 0.2f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
