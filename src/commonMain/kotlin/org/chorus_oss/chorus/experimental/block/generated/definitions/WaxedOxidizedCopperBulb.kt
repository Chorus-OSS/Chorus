package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object WaxedOxidizedCopperBulb : BlockDefinition(
    identifier = "minecraft:waxed_oxidized_copper_bulb",
    states = listOf(CommonStates.lit, CommonStates.poweredBit),
    components = listOf(
        MapColorComponent(r = 22, g = 126, b = 134, a = 255),
        LightEmissionComponent(emission = 4),
        MineableComponent(hardness = 3.0f)
    ),
    permutations = listOf(
        Permutation({ it["lit"] == false }, listOf(LightEmissionComponent(emission = 0))),
        Permutation({ it["lit"] == false && it["powered_bit"] == false }, listOf(LightEmissionComponent(emission = 0)))
    )
)
