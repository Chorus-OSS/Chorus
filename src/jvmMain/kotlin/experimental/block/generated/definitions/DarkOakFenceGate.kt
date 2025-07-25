package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object DarkOakFenceGate : BlockDefinition(
    identifier = "minecraft:dark_oak_fence_gate",
    states = listOf(CommonStates.inWallBit, CommonStates.minecraftCardinalDirection, CommonStates.openBit),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 102, g = 76, b = 51, a = 255),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 5, destroyChance = 20),
        MineableComponent(hardness = 2.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.375f),
            size = Vector3f(x = 1.0f, y = 1.0f, z = 0.25f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["minecraft:cardinal_direction"] == "west" },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.375f, y = 0.0f, z = 0.0f),
                size = Vector3f(x = 0.25f, y = 1.0f, z = 1.0f)
            )
        )
    ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "west" && it["open_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.375f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.25f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "east" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.375f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.25f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "east" && it["open_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.375f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.25f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["in_wall_bit"] == false && it["minecraft:cardinal_direction"] == "west" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.375f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.25f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["in_wall_bit"] == false && it["minecraft:cardinal_direction"] == "west" && it["open_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.375f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.25f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["in_wall_bit"] == false && it["minecraft:cardinal_direction"] == "east" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.375f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.25f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["in_wall_bit"] == false && it["minecraft:cardinal_direction"] == "east" && it["open_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.375f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.25f, y = 1.0f, z = 1.0f)
                )
            )
        )
    )
)
