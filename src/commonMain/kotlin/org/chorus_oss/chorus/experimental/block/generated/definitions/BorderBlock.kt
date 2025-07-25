package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object BorderBlock : BlockDefinition(
    identifier = "minecraft:border_block",
    states = listOf(
        CommonStates.wallConnectionTypeEast,
        CommonStates.wallConnectionTypeNorth,
        CommonStates.wallConnectionTypeSouth,
        CommonStates.wallConnectionTypeWest,
        CommonStates.wallPostBit
    ),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 255, g = 0, b = 0, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = -1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 4.9E-324f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 1.7976931348623157E308f, z = 1.0f)
        )
    )
)
