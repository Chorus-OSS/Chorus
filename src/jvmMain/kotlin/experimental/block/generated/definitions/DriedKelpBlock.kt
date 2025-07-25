package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object DriedKelpBlock : BlockDefinition(
    identifier = "minecraft:dried_kelp_block",
    components = listOf(MapColorComponent(r = 102, g = 127, b = 51, a = 255), MineableComponent(hardness = 0.5f))
)
