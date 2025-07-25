package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object GrayCandle : BlockDefinition(
    identifier = "minecraft:gray_candle",
    states = listOf(CommonStates.candles, CommonStates.lit),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 76, g = 76, b = 76, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.1f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(enabled = false)
    ),
    permutations = listOf(
        Permutation({ it["candles"] == 1 }, listOf(LightEmissionComponent(emission = 3))),
        Permutation({ it["candles"] == 2 }, listOf(LightEmissionComponent(emission = 6))),
        Permutation({ it["candles"] == 3 }, listOf(LightEmissionComponent(emission = 9)))
    )
)
