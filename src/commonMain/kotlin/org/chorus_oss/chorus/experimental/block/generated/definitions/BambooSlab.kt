package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object BambooSlab : BlockDefinition(
    identifier = "minecraft:bamboo_slab",
    states = listOf(CommonStates.minecraftVerticalHalf),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 229, g = 229, b = 51, a = 255),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 5, destroyChance = 20),
        MineableComponent(hardness = 2.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.5f, z = 1.0f)
        )
    ),
    permutations = listOf(
        Permutation(
            { it["minecraft:vertical_half"] == "top" },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.5f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.5f, z = 1.0f)
                )
            )
        )
    )
)
