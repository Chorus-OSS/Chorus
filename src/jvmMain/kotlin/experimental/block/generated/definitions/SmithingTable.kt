package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object SmithingTable : BlockDefinition(
    identifier = "minecraft:smithing_table",
    components = listOf(
        MapColorComponent(r = 143, g = 119, b = 72, a = 255),
        FlammableComponent(catchChance = 5, destroyChance = 0),
        MineableComponent(hardness = 2.5f)
    )
)
