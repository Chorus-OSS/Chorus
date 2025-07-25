package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object LitPumpkin : BlockDefinition(
    identifier = "minecraft:lit_pumpkin",
    states = listOf(CommonStates.minecraftCardinalDirection),
    components = listOf(
        MapColorComponent(r = 216, g = 127, b = 51, a = 255),
        LightEmissionComponent(emission = 15),
        MineableComponent(hardness = 1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
