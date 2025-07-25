package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object DarkOakWood : BlockDefinition(
    identifier = "minecraft:dark_oak_wood",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(
        MapColorComponent(r = 102, g = 76, b = 51, a = 255),
        FlammableComponent(catchChance = 5, destroyChance = 10),
        MineableComponent(hardness = 2.0f)
    )
)
