package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object CrackedDeepslateBricks : BlockDefinition(
    identifier = "minecraft:cracked_deepslate_bricks",
    components = listOf(MapColorComponent(r = 100, g = 100, b = 100, a = 255), MineableComponent(hardness = 3.5f))
)
