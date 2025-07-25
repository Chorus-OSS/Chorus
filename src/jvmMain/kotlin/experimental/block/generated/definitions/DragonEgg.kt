package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object DragonEgg : BlockDefinition(
    identifier = "minecraft:dragon_egg",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 25, g = 25, b = 25, a = 255),
        LightEmissionComponent(emission = 1),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
