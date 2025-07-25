package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object Vault : BlockDefinition(
    identifier = "minecraft:vault",
    states = listOf(CommonStates.minecraftCardinalDirection, CommonStates.ominous, CommonStates.vaultState),
    components = listOf(MapColorComponent(r = 112, g = 112, b = 112, a = 255))
)
