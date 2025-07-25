package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object VerdantFroglight : BlockDefinition(
    identifier = "minecraft:verdant_froglight",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(MapColorComponent(r = 127, g = 167, b = 150, a = 255), LightEmissionComponent(emission = 15))
)
