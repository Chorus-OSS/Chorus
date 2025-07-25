package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object WarpedStairs : BlockDefinition(
    identifier = "minecraft:warped_stairs",
    states = listOf(CommonStates.upsideDownBit, CommonStates.weirdoDirection),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 58, g = 142, b = 140, a = 255),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 5, destroyChance = 20),
        MineableComponent(hardness = 2.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.5f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.5f, z = 1.0f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["upside_down_bit"] == false },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                size = Vector3f(x = 1.0f, y = 0.5f, z = 1.0f)
            )
        )
    ),
        Permutation(
            { it["upside_down_bit"] == false && it["weirdo_direction"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.5f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["upside_down_bit"] == false && it["weirdo_direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.5f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["upside_down_bit"] == false && it["weirdo_direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.5f, z = 1.0f)
                )
            )
        )
    )
)
