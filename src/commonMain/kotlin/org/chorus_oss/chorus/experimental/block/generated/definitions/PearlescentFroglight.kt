package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object PearlescentFroglight : BlockDefinition(
    identifier = "minecraft:pearlescent_froglight",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(MapColorComponent(r = 242, g = 127, b = 165, a = 255), LightEmissionComponent(emission = 15))
)
