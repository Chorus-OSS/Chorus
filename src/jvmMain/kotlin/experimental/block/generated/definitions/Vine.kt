package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Vine : BlockDefinition(
    identifier = "minecraft:vine",
    states = listOf(CommonStates.vineDirectionBits),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        ReplaceableComponent,
        MineableComponent(hardness = 0.2f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.9375f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.0625f, z = 1.0f),
            enabled = false
        )
    ),
    permutations = listOf(
        Permutation(
        { it["vine_direction_bits"] == 1 },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.9375f),
                size = Vector3f(x = 1.0f, y = 1.0f, z = 0.0625f),
                enabled = false
            )
        )
    ),
        Permutation(
            { it["vine_direction_bits"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.0625f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 4 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f),
                    size = Vector3f(x = -1.0f, y = -1.0f, z = -1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 5 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.9375f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.0625f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 6 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.0625f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 7 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 8 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.9375f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.0625f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 9 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 10 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 11 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 12 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.9375f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.0625f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 13 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 14 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["vine_direction_bits"] == 15 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f),
                    enabled = false
                )
            )
        )
    )
)
