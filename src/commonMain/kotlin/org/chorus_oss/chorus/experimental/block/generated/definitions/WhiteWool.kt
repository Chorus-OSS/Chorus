package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object WhiteWool : BlockDefinition(
    identifier = "minecraft:white_wool",
    components = listOf(
        MapColorComponent(r = 255, g = 255, b = 255, a = 255),
        FlammableComponent(catchChance = 30, destroyChance = 60),
        MineableComponent(hardness = 0.8f)
    )
)
