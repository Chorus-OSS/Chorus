package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object PrismarineBricks : BlockDefinition(
    identifier = "minecraft:prismarine_bricks",
    components = listOf(MapColorComponent(r = 92, g = 219, b = 213, a = 255), MineableComponent(hardness = 1.5f))
)
