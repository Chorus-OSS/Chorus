package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object CobbledDeepslateWall : BlockDefinition(
    identifier = "minecraft:cobbled_deepslate_wall",
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
        MapColorComponent(r = 100, g = 100, b = 100, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 3.5f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 1.5f, z = 1.0f)
        )
    )
)
