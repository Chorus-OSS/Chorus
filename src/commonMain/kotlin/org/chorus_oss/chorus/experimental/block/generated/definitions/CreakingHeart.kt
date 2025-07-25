package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object CreakingHeart : BlockDefinition(
    identifier = "minecraft:creaking_heart",
    states = listOf(CommonStates.creakingHeartState, CommonStates.natural, CommonStates.pillarAxis),
    components = listOf(
        MapColorComponent(r = 216, g = 127, b = 51, a = 255),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    ),
    permutations = listOf(
        Permutation(
        { it["creaking_heart_state"] == "dormant" },
        listOf(LightEmissionComponent(emission = 15))
    ),
        Permutation(
            { it["creaking_heart_state"] == "dormant" && it["pillar_axis"] == "x" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["creaking_heart_state"] == "dormant" && it["pillar_axis"] == "z" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation({ it["creaking_heart_state"] == "awake" }, listOf(LightEmissionComponent(emission = 15))),
        Permutation(
            { it["creaking_heart_state"] == "awake" && it["pillar_axis"] == "x" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["creaking_heart_state"] == "awake" && it["pillar_axis"] == "z" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["natural"] == false && it["creaking_heart_state"] == "dormant" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["natural"] == false && it["creaking_heart_state"] == "dormant" && it["pillar_axis"] == "x" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["natural"] == false && it["creaking_heart_state"] == "dormant" && it["pillar_axis"] == "z" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["natural"] == false && it["creaking_heart_state"] == "awake" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["natural"] == false && it["creaking_heart_state"] == "awake" && it["pillar_axis"] == "x" },
            listOf(LightEmissionComponent(emission = 15))
        ),
        Permutation(
            { it["natural"] == false && it["creaking_heart_state"] == "awake" && it["pillar_axis"] == "z" },
            listOf(LightEmissionComponent(emission = 15))
        )
    )
)
