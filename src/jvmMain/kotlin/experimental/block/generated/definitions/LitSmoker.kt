package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object LitSmoker : BlockDefinition(
    identifier = "minecraft:lit_smoker",
    states = listOf(CommonStates.minecraftCardinalDirection),
    components = listOf(
        MapColorComponent(r = 112, g = 112, b = 112, a = 255),
        LightEmissionComponent(emission = 13),
        MineableComponent(hardness = 3.5f)
    )
)
