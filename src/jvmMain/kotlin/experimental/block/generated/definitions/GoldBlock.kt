package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object GoldBlock : BlockDefinition(
    identifier = "minecraft:gold_block",
    components = listOf(MapColorComponent(r = 250, g = 238, b = 77, a = 255), MineableComponent(hardness = 3.0f))
)
