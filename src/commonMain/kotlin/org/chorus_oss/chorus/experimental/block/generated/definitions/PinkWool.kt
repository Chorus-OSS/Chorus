package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object PinkWool : BlockDefinition(
    identifier = "minecraft:pink_wool",
    components = listOf(
        MapColorComponent(r = 242, g = 127, b = 165, a = 255),
        FlammableComponent(catchChance = 30, destroyChance = 60),
        MineableComponent(hardness = 0.8f)
    )
)
