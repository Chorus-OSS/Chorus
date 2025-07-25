package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object PaleOakPlanks : BlockDefinition(
    identifier = "minecraft:pale_oak_planks",
    components = listOf(
        MapColorComponent(r = 255, g = 252, b = 245, a = 255),
        FlammableComponent(catchChance = 5, destroyChance = 20),
        MineableComponent(hardness = 2.0f)
    )
)
