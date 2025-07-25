package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Grindstone : BlockDefinition(
    identifier = "minecraft:grindstone",
    states = listOf(CommonStates.attachment, CommonStates.direction),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 167, g = 167, b = 167, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 2.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.125f),
            size = Vector3f(x = 0.75f, y = 0.875f, z = 0.75f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["attachment"] == "multiple" },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.0f),
                size = Vector3f(x = 0.75f, y = 0.75f, z = 0.875f)
            )
        )
    ),
        Permutation(
            { it["attachment"] == "multiple" && it["direction"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.125f),
                    size = Vector3f(x = 0.875f, y = 0.75f, z = 0.75f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "multiple" && it["direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.125f),
                    size = Vector3f(x = 0.75f, y = 0.75f, z = 0.875f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "multiple" && it["direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.125f, z = 0.125f),
                    size = Vector3f(x = 0.875f, y = 0.75f, z = 0.75f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.0f),
                    size = Vector3f(x = 0.75f, y = 0.75f, z = 0.875f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" && it["direction"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.125f),
                    size = Vector3f(x = 0.875f, y = 0.75f, z = 0.75f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" && it["direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.125f, z = 0.125f),
                    size = Vector3f(x = 0.75f, y = 0.75f, z = 0.875f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" && it["direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.125f, z = 0.125f),
                    size = Vector3f(x = 0.875f, y = 0.75f, z = 0.75f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.0f, z = 0.125f),
                    size = Vector3f(x = 0.75f, y = 0.875f, z = 0.75f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" && it["direction"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.0f, z = 0.125f),
                    size = Vector3f(x = 0.75f, y = 0.875f, z = 0.75f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" && it["direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.0f, z = 0.125f),
                    size = Vector3f(x = 0.75f, y = 0.875f, z = 0.75f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" && it["direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.125f, y = 0.0f, z = 0.125f),
                    size = Vector3f(x = 0.75f, y = 0.875f, z = 0.75f)
                )
            )
        )
    )
)
