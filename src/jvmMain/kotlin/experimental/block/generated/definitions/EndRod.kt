package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object EndRod : BlockDefinition(
    identifier = "minecraft:end_rod",
    states = listOf(CommonStates.facingDirection),
    components = listOf(
        TransparentComponent(transparent = true),
        LightEmissionComponent(emission = 14),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.0f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.4f, y = 0.0f, z = 0.4f),
            size = Vector3f(x = 0.19999999999999996f, y = 1.0f, z = 0.19999999999999996f)
        )
    )
)
