package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Bed : BlockDefinition(
    identifier = "minecraft:bed",
    states = listOf(CommonStates.direction, CommonStates.headPieceBit, CommonStates.occupiedBit),
    components = listOf(
        TransparentComponent(transparent = true),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.2f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.5625f, z = 1.0f)
        )
    )
)
