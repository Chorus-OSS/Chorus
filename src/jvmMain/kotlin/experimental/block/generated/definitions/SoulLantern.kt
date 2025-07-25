package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object SoulLantern : BlockDefinition(
    identifier = "minecraft:soul_lantern",
    states = listOf(CommonStates.hanging),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 167, g = 167, b = 167, a = 255),
        LightEmissionComponent(emission = 10),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.5f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.3125f, y = 0.0625f, z = 0.3125f),
            size = Vector3f(x = 0.375f, y = 0.4375f, z = 0.375f)
        )
    ),
    permutations = listOf(
        Permutation(
            { it["hanging"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.3125f, y = 0.0f, z = 0.3125f),
                    size = Vector3f(x = 0.375f, y = 0.4375f, z = 0.375f)
                )
            )
        )
    )
)
