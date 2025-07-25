package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object BrewingStand : BlockDefinition(
    identifier = "minecraft:brewing_stand",
    states = listOf(
        CommonStates.brewingStandSlotABit,
        CommonStates.brewingStandSlotBBit,
        CommonStates.brewingStandSlotCBit
    ),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 167, g = 167, b = 167, a = 255),
        LightEmissionComponent(emission = 1),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.5f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.4375f, y = 0.0f, z = 0.4375f),
            size = Vector3f(x = 0.125f, y = 0.875f, z = 0.125f)
        )
    )
)
