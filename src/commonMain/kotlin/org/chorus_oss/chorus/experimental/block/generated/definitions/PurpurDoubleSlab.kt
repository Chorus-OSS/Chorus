package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object PurpurDoubleSlab : BlockDefinition(
    identifier = "minecraft:purpur_double_slab",
    states = listOf(CommonStates.minecraftVerticalHalf),
    components = listOf(MapColorComponent(r = 178, g = 76, b = 216, a = 255), MineableComponent(hardness = 2.0f))
)
