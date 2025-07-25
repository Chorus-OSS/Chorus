package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Cactus : BlockDefinition(
    identifier = "minecraft:cactus",
    states = listOf(CommonStates.age16),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 0, g = 124, b = 0, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.4f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
