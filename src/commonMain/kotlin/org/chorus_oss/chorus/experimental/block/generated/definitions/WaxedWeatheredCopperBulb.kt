package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object WaxedWeatheredCopperBulb : BlockDefinition(
    identifier = "minecraft:waxed_weathered_copper_bulb",
    states = listOf(CommonStates.lit, CommonStates.poweredBit),
    components = listOf(
        MapColorComponent(r = 58, g = 142, b = 140, a = 255),
        LightEmissionComponent(emission = 8),
        MineableComponent(hardness = 3.0f)
    ),
    permutations = listOf(
        Permutation({ it["lit"] == false }, listOf(LightEmissionComponent(emission = 0))),
        Permutation({ it["lit"] == false && it["powered_bit"] == false }, listOf(LightEmissionComponent(emission = 0)))
    )
)
