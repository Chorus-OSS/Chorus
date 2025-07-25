package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object WaxedCopperTrapdoor : BlockDefinition(
    identifier = "minecraft:waxed_copper_trapdoor",
    states = listOf(CommonStates.direction, CommonStates.openBit, CommonStates.upsideDownBit),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 167, g = 167, b = 167, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["open_bit"] == false },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0f, y = 0.8125f, z = 0.0f),
                size = Vector3f(x = 1.0f, y = 0.1875f, z = 1.0f)
            )
        )
    ),
        Permutation(
            { it["open_bit"] == false && it["upside_down_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.1875f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 1 && it["upside_down_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 1 && it["open_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.8125f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.1875f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 1 && it["open_bit"] == false && it["upside_down_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.1875f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 2 && it["upside_down_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 2 && it["open_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.8125f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.1875f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 2 && it["open_bit"] == false && it["upside_down_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.1875f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.8125f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 3 && it["upside_down_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.8125f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 3 && it["open_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.8125f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.1875f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["direction"] == 3 && it["open_bit"] == false && it["upside_down_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.1875f, z = 1.0f)
                )
            )
        )
    )
)
