package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object CalibratedSculkSensor : BlockDefinition(
    identifier = "minecraft:calibrated_sculk_sensor",
    states = listOf(CommonStates.minecraftCardinalDirection, CommonStates.sculkSensorPhase),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 13, g = 18, b = 23, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Both, sticky = false)
    )
)
