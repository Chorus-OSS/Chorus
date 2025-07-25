package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object MangroveRoots : BlockDefinition(
    identifier = "minecraft:mangrove_roots",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 129, g = 86, b = 49, a = 255),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 5, destroyChance = 0),
        MineableComponent(hardness = 0.7f)
    )
)
