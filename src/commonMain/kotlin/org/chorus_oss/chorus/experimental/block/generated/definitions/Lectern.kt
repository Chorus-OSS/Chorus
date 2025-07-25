package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Lectern : BlockDefinition(
    identifier = "minecraft:lectern",
    states = listOf(CommonStates.minecraftCardinalDirection, CommonStates.poweredBit),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 143, g = 119, b = 72, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 2.5f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.89999f, z = 1.0f)
        )
    )
)
