package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent

object GrassPath : BlockDefinition(
    identifier = "minecraft:grass_path",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 151, g = 109, b = 77, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.65f)
    )
)
