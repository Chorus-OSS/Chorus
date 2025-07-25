package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object OxidizedCopper : BlockDefinition(
    identifier = "minecraft:oxidized_copper",
    components = listOf(MapColorComponent(r = 22, g = 126, b = 134, a = 255), MineableComponent(hardness = 3.0f))
)
