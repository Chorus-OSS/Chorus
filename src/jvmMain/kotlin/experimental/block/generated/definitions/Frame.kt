package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Frame : BlockDefinition(
    identifier = "minecraft:frame",
    states = listOf(CommonStates.facingDirection, CommonStates.itemFrameMapBit, CommonStates.itemFramePhotoBit),
    components = listOf(
        TransparentComponent(transparent = true),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.25f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.125f),
            size = Vector3f(x = 0.75f, y = 0.75f, z = 0.75f),
            enabled = false
        )
    ),
    permutations = listOf(
        Permutation(
        { it["facing_direction"] == 1 },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0f, y = 0.125f, z = 0.125f),
                size = Vector3f(x = 0.0625f, y = 0.75f, z = 0.75f),
                enabled = false
            )
        )
    ),
        Permutation(
            { it["facing_direction"] == 1 && it["item_frame_photo_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.125f, z = 0.125f),
                    size = Vector3f(x = 0.0625f, y = 0.75f, z = 0.75f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 1 && it["item_frame_map_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.125f, z = 0.125f),
                    size = Vector3f(x = 0.0625f, y = 0.75f, z = 0.75f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 1 && it["item_frame_map_bit"] == false && it["item_frame_photo_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.125f, z = 0.125f),
                    size = Vector3f(x = 0.0625f, y = 0.75f, z = 0.75f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.0f),
                    size = Vector3f(x = 0.75f, y = 0.75f, z = 0.0625f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 3 && it["item_frame_photo_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.0f),
                    size = Vector3f(x = 0.75f, y = 0.75f, z = 0.0625f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 3 && it["item_frame_map_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.0f),
                    size = Vector3f(x = 0.75f, y = 0.75f, z = 0.0625f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 3 && it["item_frame_map_bit"] == false && it["item_frame_photo_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.0f),
                    size = Vector3f(x = 0.75f, y = 0.75f, z = 0.0625f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 5 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.0f, z = 0.125f),
                    size = Vector3f(x = 0.75f, y = 0.0625f, z = 0.75f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 5 && it["item_frame_photo_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.0f, z = 0.125f),
                    size = Vector3f(x = 0.75f, y = 0.0625f, z = 0.75f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 5 && it["item_frame_map_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.0f, z = 0.125f),
                    size = Vector3f(x = 0.75f, y = 0.0625f, z = 0.75f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["facing_direction"] == 5 && it["item_frame_map_bit"] == false && it["item_frame_photo_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.0f, z = 0.125f),
                    size = Vector3f(x = 0.75f, y = 0.0625f, z = 0.75f),
                    enabled = false
                )
            )
        )
    )
)
