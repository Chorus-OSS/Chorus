package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Tnt : BlockDefinition(
    identifier = "minecraft:tnt",
    states = listOf(CommonStates.explodeBit),
    components = listOf(
        MapColorComponent(r = 255, g = 0, b = 0, a = 255),
        FlammableComponent(catchChance = 15, destroyChance = 100),
        MineableComponent(hardness = 0.0f)
    )
)
