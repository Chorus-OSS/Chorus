package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object EndPortalFrame : BlockDefinition(
    identifier = "minecraft:end_portal_frame",
    states = listOf(CommonStates.endPortalEyeBit, CommonStates.minecraftCardinalDirection),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 102, g = 127, b = 51, a = 255),
        LightEmissionComponent(emission = 1),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = -1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    ),
    permutations = listOf(
        Permutation(
        { it["end_portal_eye_bit"] == false },
        listOf(
            CollisionBoxComponent(
                origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                size = Vector3f(x = 1.0f, y = 0.8125f, z = 1.0f)
            )
        )
    ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "west" && it["end_portal_eye_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.8125f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "north" && it["end_portal_eye_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.8125f, z = 1.0f)
                )
            )
        ),
        Permutation(
            { it["minecraft:cardinal_direction"] == "east" && it["end_portal_eye_bit"] == false },
            listOf(
                CollisionBoxComponent(
                    origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
                    size = Vector3f(x = 1.0f, y = 0.8125f, z = 1.0f)
                )
            )
        )
    )
)
