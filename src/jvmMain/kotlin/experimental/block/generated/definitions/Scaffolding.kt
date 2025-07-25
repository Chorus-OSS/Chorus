package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Scaffolding : BlockDefinition(
    identifier = "minecraft:scaffolding",
    states = listOf(CommonStates.stability, CommonStates.stabilityCheck),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 247, g = 233, b = 163, a = 255),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 60, destroyChance = 60),
        MineableComponent(hardness = 0.5f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.875f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.125f, z = 1.0f)
        )
    )
)
