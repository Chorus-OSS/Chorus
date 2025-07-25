package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object BlueWool : BlockDefinition(
    identifier = "minecraft:blue_wool",
    components = listOf(
        MapColorComponent(r = 51, g = 76, b = 178, a = 255),
        FlammableComponent(catchChance = 30, destroyChance = 60),
        MineableComponent(hardness = 0.8f)
    )
)
