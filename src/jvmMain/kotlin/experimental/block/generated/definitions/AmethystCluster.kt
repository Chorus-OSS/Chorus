package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object AmethystCluster : BlockDefinition(
    identifier = "minecraft:amethyst_cluster",
    states = listOf(CommonStates.minecraftBlockFace),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 153, g = 90, b = 205, a = 255),
        LightEmissionComponent(emission = 5),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 1.5f)
    )
)
