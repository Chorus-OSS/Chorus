package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object StandingBanner : BlockDefinition(
    identifier = "minecraft:standing_banner",
    states = listOf(CommonStates.groundSignDirection),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 143, g = 119, b = 72, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(enabled = false)
    )
)
