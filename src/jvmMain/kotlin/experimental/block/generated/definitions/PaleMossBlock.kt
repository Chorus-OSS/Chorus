package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object PaleMossBlock : BlockDefinition(
    identifier = "minecraft:pale_moss_block",
    components = listOf(MapColorComponent(r = 153, g = 153, b = 153, a = 255), MineableComponent(hardness = 0.1f))
)
