package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object MushroomStem : BlockDefinition(
    identifier = "minecraft:mushroom_stem",
    states = listOf(CommonStates.hugeMushroomBits),
    components = listOf(MapColorComponent(r = 199, g = 199, b = 199, a = 255), MineableComponent(hardness = 0.2f))
)
