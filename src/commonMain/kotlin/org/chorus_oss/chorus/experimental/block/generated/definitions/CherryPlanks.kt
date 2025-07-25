package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object CherryPlanks : BlockDefinition(
    identifier = "minecraft:cherry_planks",
    components = listOf(
        MapColorComponent(r = 209, g = 177, b = 161, a = 255),
        FlammableComponent(catchChance = 5, destroyChance = 20),
        MineableComponent(hardness = 2.0f)
    )
)
