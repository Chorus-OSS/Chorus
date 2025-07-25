package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object ResinBrickDoubleSlab : BlockDefinition(
    identifier = "minecraft:resin_brick_double_slab",
    states = listOf(CommonStates.minecraftVerticalHalf),
    components = listOf(MapColorComponent(r = 159, g = 82, b = 36, a = 255), MineableComponent(hardness = 2.0f))
)
