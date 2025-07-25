package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object Magma : BlockDefinition(
    identifier = "minecraft:magma",
    components = listOf(
        MapColorComponent(r = 112, g = 2, b = 0, a = 255),
        LightEmissionComponent(emission = 3),
        MineableComponent(hardness = 0.5f)
    )
)
