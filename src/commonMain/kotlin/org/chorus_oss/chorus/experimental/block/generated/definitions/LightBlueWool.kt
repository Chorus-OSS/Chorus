package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object LightBlueWool : BlockDefinition(
    identifier = "minecraft:light_blue_wool",
    components = listOf(
        MapColorComponent(r = 102, g = 153, b = 216, a = 255),
        FlammableComponent(catchChance = 30, destroyChance = 60),
        MineableComponent(hardness = 0.8f)
    )
)
