package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object JungleFenceGate : BlockDefinition(
    identifier = "minecraft:jungle_fence_gate",
    states = listOf(CommonStates.inWallBit, CommonStates.minecraftCardinalDirection, CommonStates.openBit)
)
