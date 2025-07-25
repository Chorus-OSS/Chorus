package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object JungleDoor : BlockDefinition(
    identifier = "minecraft:jungle_door",
    states = listOf(
        CommonStates.doorHingeBit,
        CommonStates.minecraftCardinalDirection,
        CommonStates.openBit,
        CommonStates.upperBlockBit
    ),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 151, g = 109, b = 77, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.8125f),
            size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["door_hinge_bit"] == false },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
            )
        )
    ),
        Permutation(
            { it["upper_block_bit"] == false && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["open_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["open_bit"] == false && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["open_bit"] == false && it["upper_block_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["open_bit"] == false && it["upper_block_bit"] == false && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "west" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "west" && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "west" && it["upper_block_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "west" && it["upper_block_bit"] == false && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "west" && it["open_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "west" && it["open_bit"] == false && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "west" && it["open_bit"] == false && it["upper_block_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "west" && it["open_bit"] == false && it["upper_block_bit"] == false && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "north" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "north" && it["upper_block_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.1875f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "north" && it["open_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "north" && it["open_bit"] == false && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "north" && it["open_bit"] == false && it["upper_block_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "north" && it["open_bit"] == false && it["upper_block_bit"] == false && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "east" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "east" && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "east" && it["upper_block_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.8125f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "east" && it["upper_block_bit"] == false && it["door_hinge_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 0.1875f, y = 1.0f, z = 1.0f)
                )
            )
        )
    )
)
