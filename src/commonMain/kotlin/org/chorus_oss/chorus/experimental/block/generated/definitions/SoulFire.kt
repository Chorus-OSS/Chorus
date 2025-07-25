package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object SoulFire : BlockDefinition(
    identifier = "minecraft:soul_fire",
    states = listOf(CommonStates.age16),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 102, g = 153, b = 216, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightEmissionComponent(emission = 15),
        LightDampeningComponent(dampening = 1),
        ReplaceableComponent,
        MineableComponent(hardness = 0.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
