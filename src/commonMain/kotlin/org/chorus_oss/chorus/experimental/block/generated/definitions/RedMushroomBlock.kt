package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object RedMushroomBlock : BlockDefinition(
    identifier = "minecraft:red_mushroom_block",
    states = listOf(CommonStates.hugeMushroomBits),
    components = listOf(MapColorComponent(r = 153, g = 51, b = 51, a = 255), MineableComponent(hardness = 0.2f))
)
