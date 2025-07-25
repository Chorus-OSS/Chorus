package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object BrownMushroom : BlockDefinition(
    identifier = "minecraft:brown_mushroom",
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 102, g = 76, b = 51, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightEmissionComponent(emission = 1),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(enabled = false)
    )
)
