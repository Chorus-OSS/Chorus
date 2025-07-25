package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object SnifferEgg : BlockDefinition(
    identifier = "minecraft:sniffer_egg",
    states = listOf(CommonStates.crackedState),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 153, g = 51, b = 51, a = 255),
        LightDampeningComponent(dampening = 1)
    )
)
