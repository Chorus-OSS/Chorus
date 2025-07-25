package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Portal : BlockDefinition(
    identifier = "minecraft:portal",
    states = listOf(CommonStates.portalAxis),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightEmissionComponent(emission = 11),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = -1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
