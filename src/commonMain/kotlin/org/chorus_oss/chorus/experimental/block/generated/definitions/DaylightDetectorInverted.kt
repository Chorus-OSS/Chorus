package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates
import org.chorus_oss.chorus.math.Vector3f

object DaylightDetectorInverted : BlockDefinition(
    identifier = "minecraft:daylight_detector_inverted",
    states = listOf(CommonStates.redstoneSignal),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 143, g = 119, b = 72, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 0.2f),
        CollisionBoxComponent(
            origin = Vector3f(x = 0.0f, y = 0.0f, z = 0.0f),
            size = Vector3f(x = 1.0f, y = 0.625f, z = 1.0f)
        )
    )
)
