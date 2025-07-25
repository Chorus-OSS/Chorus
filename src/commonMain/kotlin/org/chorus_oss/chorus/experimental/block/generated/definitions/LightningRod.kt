package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object LightningRod : BlockDefinition(
    identifier = "minecraft:lightning_rod",
    states = listOf(CommonStates.facingDirection),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 216, g = 127, b = 51, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.0f)
    )
)
