package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Loom : BlockDefinition(
    identifier = "minecraft:loom",
    states = listOf(CommonStates.direction),
    components = listOf(MapColorComponent(r = 143, g = 119, b = 72, a = 255), MineableComponent(hardness = 2.5f))
)
