package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object BeeNest : BlockDefinition(
    identifier = "minecraft:bee_nest",
    states = listOf(CommonStates.direction, CommonStates.honeyLevel),
    components = listOf(
        MapColorComponent(r = 229, g = 229, b = 51, a = 255),
        FlammableComponent(catchChance = 30, destroyChance = 60),
        MineableComponent(hardness = 0.3f)
    )
)
