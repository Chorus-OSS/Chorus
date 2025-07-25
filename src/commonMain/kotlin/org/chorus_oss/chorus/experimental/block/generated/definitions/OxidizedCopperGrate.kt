package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object OxidizedCopperGrate : BlockDefinition(
    identifier = "minecraft:oxidized_copper_grate",
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 22, g = 126, b = 134, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Both, sticky = false),
        CollisionBoxComponent(enabled = false)
    )
)
