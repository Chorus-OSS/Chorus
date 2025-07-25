package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object SpruceWallSign : BlockDefinition(
    identifier = "minecraft:spruce_wall_sign",
    states = listOf(CommonStates.facingDirection),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 129, g = 86, b = 49, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(enabled = false)
    )
)
