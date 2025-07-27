package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object DriedGhast : BlockDefinition(
    identifier = "minecraft:dried_ghast",
    states = listOf(CommonStates.minecraftCardinalDirection, CommonStates.rehydrationLevel),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 76, g = 76, b = 76, a = 255),
        LightDampeningComponent(dampening = 1)
    )
)
