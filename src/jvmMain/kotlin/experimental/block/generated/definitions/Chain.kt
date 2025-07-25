package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.CollisionBoxComponent
import org.chorus_oss.chorus.experimental.block.components.LightDampeningComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.TransparentComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Chain : BlockDefinition(
    identifier = "minecraft:chain",
    states = listOf(CommonStates.pillarAxis),
    components = listOf(
        TransparentComponent(transparent = true),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 5.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.4375f, y = 0.0f, z = 0.4375f),
            size = Vector3f(x = 0.125f, y = 1.0f, z = 0.125f)
        )
    )
)
