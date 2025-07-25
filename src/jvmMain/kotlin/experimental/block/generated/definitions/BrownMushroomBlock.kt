package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object BrownMushroomBlock : BlockDefinition(
    identifier = "minecraft:brown_mushroom_block",
    states = listOf(CommonStates.hugeMushroomBits),
    components = listOf(MapColorComponent(r = 151, g = 109, b = 77, a = 255), MineableComponent(hardness = 0.2f))
)
