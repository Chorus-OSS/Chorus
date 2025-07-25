package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object StrippedCherryWood : BlockDefinition(
    identifier = "minecraft:stripped_cherry_wood",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(
        MapColorComponent(r = 160, g = 77, b = 78, a = 255),
        FlammableComponent(catchChance = 5, destroyChance = 5),
        MineableComponent(hardness = 2.0f)
    )
)
