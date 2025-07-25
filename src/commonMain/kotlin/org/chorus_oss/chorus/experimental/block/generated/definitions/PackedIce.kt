package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FrictionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object PackedIce : BlockDefinition(
    identifier = "minecraft:packed_ice",
    components = listOf(
        MapColorComponent(r = 160, g = 160, b = 255, a = 255),
        FrictionComponent(friction = 0.98f),
        MineableComponent(hardness = 0.5f)
    )
)
