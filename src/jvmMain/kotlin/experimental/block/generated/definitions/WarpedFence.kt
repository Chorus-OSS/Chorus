package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.math.Vector3f

object WarpedFence : BlockDefinition(
    identifier = "minecraft:warped_fence",
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 76, g = 127, b = 153, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 2.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 1.5f, z = 1.0f)
        )
    )
)
