package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object BigDripleaf : BlockDefinition(
    identifier = "minecraft:big_dripleaf",
    states = listOf(
        CommonStates.bigDripleafHead,
        CommonStates.bigDripleafTilt,
        CommonStates.minecraftCardinalDirection
    ),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 0, g = 124, b = 0, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 15, destroyChance = 100),
        MineableComponent(hardness = 0.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.6875f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.3125f, z = 1.0f),
            enabled = false
        )
    ),
    permutations = listOf(
        Permutation(
        { it["big_dripleaf_tilt"] == "none" },
        listOf(InternalFrictionComponent(internalFriction = 1.0f))
    ),
        Permutation(
            { it["big_dripleaf_tilt"] == "none" && it["minecraft:cardinal_direction"] == "west" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation(
            { it["big_dripleaf_tilt"] == "none" && it["minecraft:cardinal_direction"] == "north" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation(
            { it["big_dripleaf_tilt"] == "none" && it["minecraft:cardinal_direction"] == "east" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation(
            { it["big_dripleaf_tilt"] == "partial_tilt" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation(
            { it["big_dripleaf_tilt"] == "partial_tilt" && it["minecraft:cardinal_direction"] == "west" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation(
            { it["big_dripleaf_tilt"] == "partial_tilt" && it["minecraft:cardinal_direction"] == "north" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation(
            { it["big_dripleaf_tilt"] == "partial_tilt" && it["minecraft:cardinal_direction"] == "east" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation(
            { it["big_dripleaf_tilt"] == "unstable" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation(
            { it["big_dripleaf_tilt"] == "unstable" && it["minecraft:cardinal_direction"] == "west" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation(
            { it["big_dripleaf_tilt"] == "unstable" && it["minecraft:cardinal_direction"] == "north" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation(
            { it["big_dripleaf_tilt"] == "unstable" && it["minecraft:cardinal_direction"] == "east" },
            listOf(InternalFrictionComponent(internalFriction = 1.0f))
        ),
        Permutation({ it["big_dripleaf_head"] == false }, listOf(CollisionBoxComponent(enabled = false))),
        Permutation(
            { it["big_dripleaf_head"] == false && it["minecraft:cardinal_direction"] == "west" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["minecraft:cardinal_direction"] == "north" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["minecraft:cardinal_direction"] == "east" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "none" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "none" && it["minecraft:cardinal_direction"] == "west" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "none" && it["minecraft:cardinal_direction"] == "north" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "none" && it["minecraft:cardinal_direction"] == "east" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "partial_tilt" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "partial_tilt" && it["minecraft:cardinal_direction"] == "west" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "partial_tilt" && it["minecraft:cardinal_direction"] == "north" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "partial_tilt" && it["minecraft:cardinal_direction"] == "east" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "unstable" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "unstable" && it["minecraft:cardinal_direction"] == "west" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "unstable" && it["minecraft:cardinal_direction"] == "north" },
            listOf(CollisionBoxComponent(enabled = false))
        ),
        Permutation(
            { it["big_dripleaf_head"] == false && it["big_dripleaf_tilt"] == "unstable" && it["minecraft:cardinal_direction"] == "east" },
            listOf(CollisionBoxComponent(enabled = false))
        )
    )
)
