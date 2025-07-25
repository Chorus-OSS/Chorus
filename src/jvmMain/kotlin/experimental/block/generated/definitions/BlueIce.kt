package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FrictionComponent
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object BlueIce : BlockDefinition(
    identifier = "minecraft:blue_ice",
    components = listOf(
        MapColorComponent(r = 160, g = 160, b = 255, a = 255),
        FrictionComponent(friction = 0.989f),
        LightEmissionComponent(emission = 4),
        MineableComponent(hardness = 2.8f)
    )
)
