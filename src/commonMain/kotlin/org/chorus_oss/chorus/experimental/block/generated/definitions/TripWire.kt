package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object TripWire : BlockDefinition(
    identifier = "minecraft:trip_wire",
    states = listOf(
        CommonStates.attachedBit,
        CommonStates.disarmedBit,
        CommonStates.poweredBit,
        CommonStates.suspendedBit
    ),
    components = listOf(
        TransparentComponent(transparent = true),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.5f, z = 1.0f),
            enabled = false
        )
    )
)
