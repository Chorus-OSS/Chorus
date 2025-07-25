package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object Mud : BlockDefinition(
    identifier = "minecraft:mud",
    components = listOf(MapColorComponent(r = 87, g = 92, b = 92, a = 255), MineableComponent(hardness = 0.5f))
)
