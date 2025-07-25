package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object PaleMossCarpet : BlockDefinition(
    identifier = "minecraft:pale_moss_carpet",
    states = listOf(
        CommonStates.paleMossCarpetSideEast,
        CommonStates.paleMossCarpetSideNorth,
        CommonStates.paleMossCarpetSideSouth,
        CommonStates.paleMossCarpetSideWest,
        CommonStates.upperBlockBit
    ),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 153, g = 153, b = 153, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.1f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.0625f, z = 1.0f)
        )
    )
)
