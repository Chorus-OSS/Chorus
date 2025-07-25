package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object Ice : BlockDefinition(
    identifier = "minecraft:ice",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 160, g = 160, b = 255, a = 255),
        FrictionComponent(friction = 0.98f),
        LightDampeningComponent(dampening = 2),
        FlammableComponent(catchChance = -1, destroyChance = 0),
        MineableComponent(hardness = 0.5f)
    )
)
