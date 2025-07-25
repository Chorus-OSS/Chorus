package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.SolidComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent

object IronBars : BlockDefinition(
    identifier = "minecraft:iron_bars",
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 5.0f)
    )
)
