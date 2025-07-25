package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object WarpedFungus : BlockDefinition(
    identifier = "minecraft:warped_fungus",
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 76, g = 127, b = 153, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(enabled = false)
    )
)
