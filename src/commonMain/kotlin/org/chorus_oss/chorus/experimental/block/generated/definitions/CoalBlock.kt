package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object CoalBlock : BlockDefinition(
    identifier = "minecraft:coal_block",
    components = listOf(
        MapColorComponent(r = 25, g = 25, b = 25, a = 255),
        FlammableComponent(catchChance = 5, destroyChance = 5),
        MineableComponent(hardness = 5.0f)
    )
)
