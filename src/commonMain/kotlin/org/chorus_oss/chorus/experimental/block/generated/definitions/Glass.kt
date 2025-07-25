package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent

object Glass : BlockDefinition(
    identifier = "minecraft:glass",
    components = listOf(
        TransparentComponent(transparent = true),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.3f),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 153, g = 153, b = 153, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.3f)
    )
)
