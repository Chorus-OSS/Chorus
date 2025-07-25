package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object TurtleEgg : BlockDefinition(
    identifier = "minecraft:turtle_egg",
    states = listOf(CommonStates.crackedState, CommonStates.turtleEggCount),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 247, g = 233, b = 163, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.5f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.1875f, y = 0.0f, z = 0.1875f),
            size = Vector3f(x = 0.5625f, y = 0.4375f, z = 0.5625f)
        )
    )
)
