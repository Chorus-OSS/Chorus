package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object Slime : BlockDefinition(
    identifier = "minecraft:slime",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 127, g = 178, b = 56, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Both, sticky = true)
    )
)
