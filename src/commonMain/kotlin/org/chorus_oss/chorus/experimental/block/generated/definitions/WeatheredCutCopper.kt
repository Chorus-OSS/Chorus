package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object WeatheredCutCopper : BlockDefinition(
    identifier = "minecraft:weathered_cut_copper",
    components = listOf(MapColorComponent(r = 58, g = 142, b = 140, a = 255), MineableComponent(hardness = 3.0f))
)
