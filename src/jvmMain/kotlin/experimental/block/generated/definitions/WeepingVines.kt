package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object WeepingVines : BlockDefinition(
    identifier = "minecraft:weeping_vines",
    states = listOf(CommonStates.weepingVinesAge),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 112, g = 2, b = 0, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.25f, y = 0.0f, z = 0.25f),
            size = Vector3f(x = 0.5f, y = 0.9375f, z = 0.5f),
            enabled = false
        )
    )
)
