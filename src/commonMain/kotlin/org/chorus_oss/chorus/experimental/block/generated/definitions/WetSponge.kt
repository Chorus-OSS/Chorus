package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object WetSponge : BlockDefinition(
    identifier = "minecraft:wet_sponge",
    components = listOf(MapColorComponent(r = 229, g = 229, b = 51, a = 255), MineableComponent(hardness = 0.6f))
)
