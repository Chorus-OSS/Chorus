package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object Beacon : BlockDefinition(
    identifier = "minecraft:beacon",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 92, g = 219, b = 213, a = 255),
        LightEmissionComponent(emission = 15),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.0f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
