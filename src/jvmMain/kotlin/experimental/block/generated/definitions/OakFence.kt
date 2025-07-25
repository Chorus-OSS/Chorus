package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.math.Vector3f

object OakFence : BlockDefinition(
    identifier = "minecraft:oak_fence",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 143, g = 119, b = 72, a = 255),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 5, destroyChance = 20),
        MineableComponent(hardness = 2.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 1.5f, z = 1.0f)
        )
    )
)
