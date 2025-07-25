package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object HardenedClay : BlockDefinition(
    identifier = "minecraft:hardened_clay",
    components = listOf(
        MapColorComponent(r = 216, g = 127, b = 51, a = 255),
        MineableComponent(hardness = 1.25f),
        MapColorComponent(r = 135, g = 107, b = 98, a = 255),
        MineableComponent(hardness = 1.25f)
    )
)
