package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object PaleOakPressurePlate : BlockDefinition(
    identifier = "minecraft:pale_oak_pressure_plate",
    states = listOf(CommonStates.redstoneSignal),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 255, g = 252, b = 245, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.5f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0625f, y = 0.0f, z = 0.0625f),
            size = Vector3f(x = 0.875f, y = 0.25f, z = 0.875f),
            enabled = false
        )
    )
)
