package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object CandleCake : BlockDefinition(
    identifier = "minecraft:candle_cake",
    states = listOf(CommonStates.lit),
    components = listOf(
        TransparentComponent(transparent = true),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.5f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.1875f, y = 0.0f, z = 0.0625f),
            size = Vector3f(x = 0.75f, y = 0.5f, z = 0.875f)
        ),
        TransparentComponent(transparent = true),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.5f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.1875f, y = 0.0f, z = 0.0625f),
            size = Vector3f(x = 0.75f, y = 0.5f, z = 0.875f)
        )
    ),
    permutations = listOf(
        Permutation(
            { it["lit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0625f, y = 0.0f, z = 0.0625f),
                    size = Vector3f(x = 0.875f, y = 0.5f, z = 0.875f)
                )
            )
        )
    )
)
