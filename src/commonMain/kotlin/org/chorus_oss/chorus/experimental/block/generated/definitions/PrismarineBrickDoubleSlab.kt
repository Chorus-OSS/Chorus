package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object PrismarineBrickDoubleSlab : BlockDefinition(
    identifier = "minecraft:prismarine_brick_double_slab",
    states = listOf(CommonStates.minecraftVerticalHalf),
    components = listOf(MapColorComponent(r = 92, g = 219, b = 213, a = 255), MineableComponent(hardness = 2.0f))
)
