package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.FlammableComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object CherryLog : BlockDefinition(
    identifier = "minecraft:cherry_log",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(
        MapColorComponent(r = 209, g = 177, b = 161, a = 255),
        FlammableComponent(catchChance = 5, destroyChance = 5),
        MineableComponent(hardness = 2.0f)
    ),
    permutations = listOf(
        Permutation(
        { it["pillar_axis"] == "x" },
        listOf(MapColorComponent(r = 57, g = 41, b = 35, a = 255))
    ), Permutation({ it["pillar_axis"] == "z" }, listOf(MapColorComponent(r = 57, g = 41, b = 35, a = 255)))
    )
)
