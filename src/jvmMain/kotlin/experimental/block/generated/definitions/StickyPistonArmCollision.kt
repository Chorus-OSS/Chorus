package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object StickyPistonArmCollision : BlockDefinition(
    identifier = "minecraft:sticky_piston_arm_collision",
    states = listOf(CommonStates.facingDirection),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 112, g = 112, b = 112, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 1.5f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
