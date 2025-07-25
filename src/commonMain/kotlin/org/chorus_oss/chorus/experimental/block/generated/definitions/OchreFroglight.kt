package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object OchreFroglight : BlockDefinition(
    identifier = "minecraft:ochre_froglight",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(MapColorComponent(r = 247, g = 233, b = 163, a = 255), LightEmissionComponent(emission = 15))
)
