package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Ladder : BlockDefinition(
    identifier = "minecraft:ladder",
    states = listOf(CommonStates.facingDirection),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.4f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 1.0f),
            size = Vector3f(x = 1.0f, y = 1.0f, z = 0.0f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["facing_direction"] == 2 },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.8125f),
                size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
            )
        )
    ),
        Permutation(
            { it["facing_direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 4 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 5 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        )
    )
)
