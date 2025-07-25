package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.math.Vector3f

object Conduit : BlockDefinition(
    identifier = "minecraft:conduit",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 92, g = 219, b = 213, a = 255),
        LightEmissionComponent(emission = 15),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.3125f, y = 0.3125f, z = 0.3125f),
            size = Vector3f(x = 0.375f, y = 0.375f, z = 0.375f)
        )
    )
)
