package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object ExposedCopperBulb : BlockDefinition(
    identifier = "minecraft:exposed_copper_bulb",
    states = listOf(CommonStates.lit, CommonStates.poweredBit),
    components = listOf(
        MapColorComponent(r = 135, g = 107, b = 98, a = 255),
        LightEmissionComponent(emission = 12),
        MineableComponent(hardness = 3.0f)
    ),
    permutations = listOf(
        Permutation({ it["lit"] == false }, listOf(LightEmissionComponent(emission = 0))),
        Permutation({ it["lit"] == false && it["powered_bit"] == false }, listOf(LightEmissionComponent(emission = 0)))
    )
)
