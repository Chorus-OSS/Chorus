package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object PaleOakLog : BlockDefinition(
    identifier = "minecraft:pale_oak_log",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(
        MapColorComponent(r = 255, g = 252, b = 245, a = 255),
        FlammableComponent(catchChance = 5, destroyChance = 10),
        MineableComponent(hardness = 2.0f)
    ),
    permutations = listOf(
        Permutation(
        { it["pillar_axis"] == "x" },
        listOf(MapColorComponent(r = 112, g = 112, b = 112, a = 255))
    ), Permutation({ it["pillar_axis"] == "z" }, listOf(MapColorComponent(r = 112, g = 112, b = 112, a = 255)))
    )
)
