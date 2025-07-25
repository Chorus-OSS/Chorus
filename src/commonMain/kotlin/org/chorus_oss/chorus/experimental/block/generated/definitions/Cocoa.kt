package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Cocoa : BlockDefinition(
    identifier = "minecraft:cocoa",
    states = listOf(CommonStates.age3, CommonStates.direction),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 0, g = 124, b = 0, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.2f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.375f, y = 0.4375f, z = 0.6875f),
            size = Vector3f(x = 0.25f, y = 0.3125f, z = 0.25f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["direction"] == 1 },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0625f, y = 0.4375f, z = 0.375f),
                size = Vector3f(x = 0.25f, y = 0.3125f, z = 0.25f)
            )
        )
    ),
        Permutation(
            { it["direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.375f, y = 0.4375f, z = 0.0625f),
                    size = Vector3f(x = 0.25f, y = 0.3125f, z = 0.25f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.6875f, y = 0.4375f, z = 0.375f),
                    size = Vector3f(x = 0.25f, y = 0.3125f, z = 0.25f)
                )
            )
        ),
        Permutation(
            { it["age"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.3125f, y = 0.3125f, z = 0.5625f),
                    size = Vector3f(x = 0.375f, y = 0.4375f, z = 0.375f)
                )
            )
        ),
        Permutation(
            { it["age"] == 1 && it["direction"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0625f, y = 0.3125f, z = 0.3125f),
                    size = Vector3f(x = 0.375f, y = 0.4375f, z = 0.375f)
                )
            )
        ),
        Permutation(
            { it["age"] == 1 && it["direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.3125f, y = 0.3125f, z = 0.0625f),
                    size = Vector3f(x = 0.375f, y = 0.4375f, z = 0.375f)
                )
            )
        ),
        Permutation(
            { it["age"] == 1 && it["direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.5625f, y = 0.3125f, z = 0.3125f),
                    size = Vector3f(x = 0.375f, y = 0.4375f, z = 0.375f)
                )
            )
        ),
        Permutation(
            { it["age"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.3125f, y = 0.3125f, z = 0.5625f),
                    size = Vector3f(x = 0.375f, y = 0.4375f, z = 0.375f)
                )
            )
        ),
        Permutation(
            { it["age"] == 2 && it["direction"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0625f, y = 0.3125f, z = 0.3125f),
                    size = Vector3f(x = 0.375f, y = 0.4375f, z = 0.375f)
                )
            )
        ),
        Permutation(
            { it["age"] == 2 && it["direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.3125f, y = 0.3125f, z = 0.0625f),
                    size = Vector3f(x = 0.375f, y = 0.4375f, z = 0.375f)
                )
            )
        ),
        Permutation(
            { it["age"] == 2 && it["direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.5625f, y = 0.3125f, z = 0.3125f),
                    size = Vector3f(x = 0.375f, y = 0.4375f, z = 0.375f)
                )
            )
        )
    )
)
