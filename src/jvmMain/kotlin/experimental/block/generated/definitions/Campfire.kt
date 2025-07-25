package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Campfire : BlockDefinition(
    identifier = "minecraft:campfire",
    states = listOf(CommonStates.extinguished, CommonStates.minecraftCardinalDirection),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 129, g = 86, b = 49, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 5.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    ),
    permutations = listOf(
        Permutation({ it["extinguished"] == false }, listOf(LightEmissionComponent(emission = 15))),
        Permutation(
            { it["extinguished"] == false && it["minecraft:cardinal_direction"] == "west" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["extinguished"] == false && it["minecraft:cardinal_direction"] == "north" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["extinguished"] == false && it["minecraft:cardinal_direction"] == "east" },
            listOf(LightEmissionComponent(emission = 15))
        )
    )
)
