package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent

object GrayStainedGlass : BlockDefinition(
    identifier = "minecraft:gray_stained_glass",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 76, g = 76, b = 76, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.3f)
    )
)
