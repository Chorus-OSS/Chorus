package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object DeepslateDiamondOre : BlockDefinition(
    identifier = "minecraft:deepslate_diamond_ore",
    components = listOf(MapColorComponent(r = 100, g = 100, b = 100, a = 255), MineableComponent(hardness = 4.5f))
)
