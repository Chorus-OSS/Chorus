package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.SolidComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent

object LightBlueStainedGlassPane : BlockDefinition(
    identifier = "minecraft:light_blue_stained_glass_pane",
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.3f)
    )
)
