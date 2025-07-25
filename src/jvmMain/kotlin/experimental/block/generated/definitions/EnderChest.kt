package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object EnderChest : BlockDefinition(
    identifier = "minecraft:ender_chest",
    states = listOf(CommonStates.minecraftCardinalDirection),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 112, g = 112, b = 112, a = 255),
        LightEmissionComponent(emission = 7),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 22.5f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0625f, y = 0.0f, z = 0.0625f),
            size = Vector3f(x = 0.875f, y = 0.9475f, z = 0.875f)
        )
    )
)
