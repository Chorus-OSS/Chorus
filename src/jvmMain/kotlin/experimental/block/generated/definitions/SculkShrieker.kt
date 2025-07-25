package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object SculkShrieker : BlockDefinition(
    identifier = "minecraft:sculk_shrieker",
    states = listOf(CommonStates.active, CommonStates.canSummon),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 13, g = 18, b = 23, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.0f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
