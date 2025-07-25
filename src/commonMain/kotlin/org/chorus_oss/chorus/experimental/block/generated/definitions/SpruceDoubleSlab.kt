package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object SpruceDoubleSlab : BlockDefinition(
    identifier = "minecraft:spruce_double_slab",
    states = listOf(CommonStates.minecraftVerticalHalf),
    components = listOf(MapColorComponent(r = 129, g = 86, b = 49, a = 255), MineableComponent(hardness = 2.0f))
)
