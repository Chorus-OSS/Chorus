package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Chest : BlockDefinition(
    identifier = "minecraft:chest",
    states = listOf(CommonStates.minecraftCardinalDirection),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 143, g = 119, b = 72, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 2.5f),
        TODO("MoveableComponent"),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0625f, y = 0.0f, z = 0.0625f),
            size = Vector3f(x = 0.875f, y = 0.9475f, z = 0.875f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["minecraft:cardinal_direction"] == "west" },
        listOf(TODO("MoveableComponent"))
    ),
        Permutation({ it["minecraft:cardinal_direction"] == "north" }, listOf(TODO("MoveableComponent"))),
        Permutation({ it["minecraft:cardinal_direction"] == "east" }, listOf(TODO("MoveableComponent")))
    )
)
