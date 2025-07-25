package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object SnowLayer : BlockDefinition(
    identifier = "minecraft:snow_layer",
    states = listOf(CommonStates.coveredBit, CommonStates.height),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 255, g = 255, b = 255, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        ReplaceableComponent,
        MineableComponent(hardness = 0.2f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.125f, z = 1.0f),
            enabled = false
        )
    ),
    permutations = listOf(
        Permutation(
        { it["height"] == 1 },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                size = Vector3f(x = 1.0f, y = 0.25f, z = 1.0f),
                enabled = false
            )
        )
    ),
        Permutation(
            { it["height"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.375f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["height"] == 3 },
            listOf(
                InternalFrictionComponent(internalFriction = 1.0f),
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.5f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["height"] == 4 },
            listOf(
                InternalFrictionComponent(internalFriction = 1.0f),
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.625f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["height"] == 5 },
            listOf(
                InternalFrictionComponent(internalFriction = 1.0f),
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.75f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["height"] == 6 },
            listOf(
                InternalFrictionComponent(internalFriction = 1.0f),
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.875f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["height"] == 7 },
            listOf(
                InternalFrictionComponent(internalFriction = 1.0f),
                ReplaceableComponent,
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["covered_bit"] == false && it["height"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.25f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["covered_bit"] == false && it["height"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.375f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["covered_bit"] == false && it["height"] == 3 },
            listOf(
                InternalFrictionComponent(internalFriction = 1.0f),
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.5f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["covered_bit"] == false && it["height"] == 4 },
            listOf(
                InternalFrictionComponent(internalFriction = 1.0f),
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.625f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["covered_bit"] == false && it["height"] == 5 },
            listOf(
                InternalFrictionComponent(internalFriction = 1.0f),
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.75f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["covered_bit"] == false && it["height"] == 6 },
            listOf(
                InternalFrictionComponent(internalFriction = 1.0f),
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.875f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["covered_bit"] == false && it["height"] == 7 },
            listOf(
                InternalFrictionComponent(internalFriction = 1.0f),
                ReplaceableComponent,
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 1.0f)
                )
            )
        )
    )
)
