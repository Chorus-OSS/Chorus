package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Candle : BlockDefinition(
    identifier = "minecraft:candle",
    states = listOf(CommonStates.candles, CommonStates.lit),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 247, g = 233, b = 163, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.1f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(enabled = false),
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 153, g = 153, b = 153, a = 255),
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
