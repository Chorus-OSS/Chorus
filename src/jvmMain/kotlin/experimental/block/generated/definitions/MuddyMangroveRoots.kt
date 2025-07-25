package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object MuddyMangroveRoots : BlockDefinition(
    identifier = "minecraft:muddy_mangrove_roots",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 129, g = 86, b = 49, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.7f)
    )
)
