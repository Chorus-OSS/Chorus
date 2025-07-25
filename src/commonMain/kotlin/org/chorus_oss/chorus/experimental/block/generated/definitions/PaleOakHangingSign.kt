package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object PaleOakHangingSign : BlockDefinition(
    identifier = "minecraft:pale_oak_hanging_sign",
    states = listOf(
        CommonStates.attachedBit,
        CommonStates.facingDirection,
        CommonStates.groundSignDirection,
        CommonStates.hanging
    ),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 255, g = 252, b = 245, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(enabled = false)
    )
)
