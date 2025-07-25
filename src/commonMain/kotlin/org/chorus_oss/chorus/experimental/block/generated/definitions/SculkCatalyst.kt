package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object SculkCatalyst : BlockDefinition(
    identifier = "minecraft:sculk_catalyst",
    states = listOf(CommonStates.bloom),
    components = listOf(
        MapColorComponent(r = 13, g = 18, b = 23, a = 255),
        LightEmissionComponent(emission = 6),
        MineableComponent(hardness = 3.0f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
