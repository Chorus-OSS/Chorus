package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object ChiseledRedSandstone : BlockDefinition(
    identifier = "minecraft:chiseled_red_sandstone",
    components = listOf(MapColorComponent(r = 216, g = 127, b = 51, a = 255), MineableComponent(hardness = 0.8f))
)
