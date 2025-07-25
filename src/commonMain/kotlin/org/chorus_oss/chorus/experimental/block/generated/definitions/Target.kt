package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object Target : BlockDefinition(
    identifier = "minecraft:target",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 255, g = 255, b = 255, a = 255),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 0, destroyChance = 15),
        MineableComponent(hardness = 0.5f)
    )
)
