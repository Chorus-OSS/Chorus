package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object Lava : BlockDefinition(
    identifier = "minecraft:lava",
    states = listOf(CommonStates.liquidDepth),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 255, g = 0, b = 0, a = 255),
        InternalFrictionComponent(internalFriction = 0.3f),
        LightEmissionComponent(emission = 15),
        LightDampeningComponent(dampening = 2),
        ReplaceableComponent,
        MineableComponent(hardness = 100.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.8888888880610466f, z = 1.0f),
            enabled = false
        )
    ),
    permutations = listOf(
        Permutation(
        { it["liquid_depth"] == 1 },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                size = Vector3f(x = 1.0f, y = 0.7777777761220932f, z = 1.0f),
                enabled = false
            )
        )
    ),
        Permutation(
            { it["liquid_depth"] == 2 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.6666666567325592f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["liquid_depth"] == 3 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.5555555522441864f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["liquid_depth"] == 4 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.4444444179534912f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["liquid_depth"] == 5 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.3333333134651184f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["liquid_depth"] == 6 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.2222222089767456f, z = 1.0f),
                    enabled = false
                )
            )
        ),
        Permutation(
            { it["liquid_depth"] == 7 },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.1111111044883728f, z = 1.0f),
                    enabled = false
                )
            )
        )
    )
)
