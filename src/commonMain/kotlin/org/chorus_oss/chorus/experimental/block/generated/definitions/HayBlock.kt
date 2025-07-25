package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object HayBlock : BlockDefinition(
    identifier = "minecraft:hay_block",
    states = listOf(CommonStates.deprecated, CommonStates.pillarAxis),
    components = listOf(
        MapColorComponent(r = 229, g = 229, b = 51, a = 255),
        FlammableComponent(catchChance = 60, destroyChance = 20),
        MineableComponent(hardness = 0.5f)
    )
)
