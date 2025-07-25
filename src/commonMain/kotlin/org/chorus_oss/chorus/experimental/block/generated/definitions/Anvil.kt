package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Anvil : BlockDefinition(
    identifier = "minecraft:anvil",
    states = listOf(CommonStates.minecraftCardinalDirection),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 167, g = 167, b = 167, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 5.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.125f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 0.75f, y = 1.0f, z = 1.0f)
        )
    ),
    permutations = listOf(
        Permutation(
        { it["minecraft:cardinal_direction"] == "west" },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.125f),
                size = Vector3f(x = 1.0f, y = 1.0f, z = 0.75f)
            )
        )
    ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "east" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.125f),
                    size = Vector3f(x = 1.0f, y = 1.0f, z = 0.75f)
                )
            )
        )
    )
)
