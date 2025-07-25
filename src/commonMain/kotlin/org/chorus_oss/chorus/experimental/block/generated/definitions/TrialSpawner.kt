package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object TrialSpawner : BlockDefinition(
    identifier = "minecraft:trial_spawner",
    states = listOf(CommonStates.ominous, CommonStates.trialSpawnerState),
    components = listOf(MapColorComponent(r = 112, g = 112, b = 112, a = 255))
)
