package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Bell : BlockDefinition(
    identifier = "minecraft:bell",
    states = listOf(CommonStates.attachment, CommonStates.direction, CommonStates.toggleBit),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 250, g = 238, b = 77, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 1.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.249999f, y = 0.249999f, z = 0.249999f),
            size = Vector3f(x = 0.5000020000000001f, y = 0.750002f, z = 0.5000020000000001f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["attachment"] == "multiple" },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.249999f, y = 0.249999f, z = -1.0E-6f),
                size = Vector3f(x = 0.5000020000000001f, y = 0.5000020000000001f, z = 1.0000019999999998f)
            )
        )
    ),
        Permutation(
            { it["attachment"] == "multiple" && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = 0.249999f, z = -1.0E-6f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.5000020000000001f, z = 1.0000019999999998f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "multiple" && it["direction"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = -1.0E-6f, y = 0.249999f, z = 0.249999f),
                    size = Vector3f(x = 1.0000019999999998f, y = 0.5000020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "multiple" && it["direction"] == 1 && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = -1.0E-6f, y = 0.249999f, z = 0.249999f),
                    size = Vector3f(x = 1.0000019999999998f, y = 0.5000020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "multiple" && it["direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = 0.249999f, z = -1.0E-6f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.5000020000000001f, z = 1.0000019999999998f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "multiple" && it["direction"] == 2 && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = 0.249999f, z = -1.0E-6f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.5000020000000001f, z = 1.0000019999999998f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "multiple" && it["direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = -1.0E-6f, y = 0.249999f, z = 0.249999f),
                    size = Vector3f(x = 1.0000019999999998f, y = 0.5000020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "multiple" && it["direction"] == 3 && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = -1.0E-6f, y = 0.249999f, z = 0.249999f),
                    size = Vector3f(x = 1.0000019999999998f, y = 0.5000020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = 0.249999f, z = -1.0E-6f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.5000020000000001f, z = 0.7500020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = 0.249999f, z = -1.0E-6f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.5000020000000001f, z = 0.7500020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" && it["direction"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = 0.249999f, z = 0.249999f),
                    size = Vector3f(x = 0.750002f, y = 0.5000020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" && it["direction"] == 1 && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = 0.249999f, z = 0.249999f),
                    size = Vector3f(x = 0.750002f, y = 0.5000020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" && it["direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = 0.249999f, z = 0.249999f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.5000020000000001f, z = 0.750002f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" && it["direction"] == 2 && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = 0.249999f, z = 0.249999f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.5000020000000001f, z = 0.750002f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" && it["direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = -1.0E-6f, y = 0.249999f, z = 0.249999f),
                    size = Vector3f(x = 0.7500020000000001f, y = 0.5000020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "side" && it["direction"] == 3 && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = -1.0E-6f, y = 0.249999f, z = 0.249999f),
                    size = Vector3f(x = 0.7500020000000001f, y = 0.5000020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = -1.0E-6f, y = -1.0E-6f, z = 0.249999f),
                    size = Vector3f(x = 1.0000019999999998f, y = 0.7500020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = -1.0E-6f, y = -1.0E-6f, z = 0.249999f),
                    size = Vector3f(x = 1.0000019999999998f, y = 0.7500020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" && it["direction"] == 1 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = -1.0E-6f, z = -1.0E-6f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.7500020000000001f, z = 1.0000019999999998f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" && it["direction"] == 1 && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = -1.0E-6f, z = -1.0E-6f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.7500020000000001f, z = 1.0000019999999998f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" && it["direction"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = -1.0E-6f, y = -1.0E-6f, z = 0.249999f),
                    size = Vector3f(x = 1.0000019999999998f, y = 0.7500020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" && it["direction"] == 2 && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = -1.0E-6f, y = -1.0E-6f, z = 0.249999f),
                    size = Vector3f(x = 1.0000019999999998f, y = 0.7500020000000001f, z = 0.5000020000000001f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" && it["direction"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = -1.0E-6f, z = -1.0E-6f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.7500020000000001f, z = 1.0000019999999998f)
                )
            )
        ),
        Permutation(
            { it["attachment"] == "standing" && it["direction"] == 3 && it["toggle_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.249999f, y = -1.0E-6f, z = -1.0E-6f),
                    size = Vector3f(x = 0.5000020000000001f, y = 0.7500020000000001f, z = 1.0000019999999998f)
                )
            )
        )
    )
)
