package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object ChorusFlower : BlockDefinition(
    identifier = "minecraft:chorus_flower",
    states = listOf(CommonStates.age6),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 153, g = 90, b = 205, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.4f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
