package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Farmland : BlockDefinition(
    identifier = "minecraft:farmland",
    states = listOf(CommonStates.moisturizedAmount),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 151, g = 109, b = 77, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.6f)
    )
)
