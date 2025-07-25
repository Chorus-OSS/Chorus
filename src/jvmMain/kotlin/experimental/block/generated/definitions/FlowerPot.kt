package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object FlowerPot : BlockDefinition(
    identifier = "minecraft:flower_pot",
    states = listOf(CommonStates.updateBit),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.3125f, y = 0.0f, z = 0.3125f),
            size = Vector3f(x = 0.375f, y = 0.375f, z = 0.375f)
        )
    )
)
