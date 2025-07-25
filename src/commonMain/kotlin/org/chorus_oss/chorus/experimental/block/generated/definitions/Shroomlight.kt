package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object Shroomlight : BlockDefinition(
    identifier = "minecraft:shroomlight",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 153, g = 51, b = 51, a = 255),
        LightEmissionComponent(emission = 15),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 1.0f)
    )
)
