package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object Bookshelf : BlockDefinition(
    identifier = "minecraft:bookshelf",
    components = listOf(
        MapColorComponent(r = 143, g = 119, b = 72, a = 255),
        FlammableComponent(catchChance = 30, destroyChance = 20),
        MineableComponent(hardness = 1.5f)
    )
)
