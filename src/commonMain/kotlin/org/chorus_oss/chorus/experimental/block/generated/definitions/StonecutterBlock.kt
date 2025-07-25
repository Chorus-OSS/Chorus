package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object StonecutterBlock : BlockDefinition(
    identifier = "minecraft:stonecutter_block",
    states = listOf(CommonStates.minecraftCardinalDirection),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 112, g = 112, b = 112, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.5f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.5625f, z = 1.0f)
        )
    )
)
