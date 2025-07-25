package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object Glowstone : BlockDefinition(
    identifier = "minecraft:glowstone",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 247, g = 233, b = 163, a = 255),
        LightEmissionComponent(emission = 15),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.3f)
    )
)
