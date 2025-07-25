package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object WeatheredCopperGrate : BlockDefinition(
    identifier = "minecraft:weathered_copper_grate",
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 58, g = 142, b = 140, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Both, sticky = false),
        CollisionBoxComponent(enabled = false)
    )
)
