package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.SolidComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object TripwireHook : BlockDefinition(
    identifier = "minecraft:tripwire_hook",
    states = listOf(CommonStates.attachedBit, CommonStates.direction, CommonStates.poweredBit),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        LightDampeningComponent(dampening = 1)
    )
)
