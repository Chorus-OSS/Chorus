package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.math.Vector3f

object HeavyCore : BlockDefinition(
    identifier = "minecraft:heavy_core",
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 167, g = 167, b = 167, a = 255),
        LightDampeningComponent(dampening = 1),
        MoveableComponent(movement = MoveableComponent.Movement.Both, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.25f, y = 0.0f, z = 0.25f),
            size = Vector3f(x = 0.5f, y = 0.5f, z = 0.5f)
        )
    )
)
