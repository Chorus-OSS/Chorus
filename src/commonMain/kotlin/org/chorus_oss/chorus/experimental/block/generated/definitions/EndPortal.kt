package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.math.Vector3f

object EndPortal : BlockDefinition(
    identifier = "minecraft:end_portal",
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 25, g = 25, b = 25, a = 255),
        LightEmissionComponent(emission = 15),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = -1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.75f, z = 1.0f)
        )
    )
)
