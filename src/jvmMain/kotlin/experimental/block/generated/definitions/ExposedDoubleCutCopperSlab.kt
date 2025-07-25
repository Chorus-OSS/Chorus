package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object ExposedDoubleCutCopperSlab : BlockDefinition(
    identifier = "minecraft:exposed_double_cut_copper_slab",
    states = listOf(CommonStates.minecraftVerticalHalf),
    components = listOf(MapColorComponent(r = 135, g = 107, b = 98, a = 255), MineableComponent(hardness = 3.0f))
)
