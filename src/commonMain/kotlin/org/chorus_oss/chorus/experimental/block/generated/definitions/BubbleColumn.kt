package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object BubbleColumn : BlockDefinition(
    identifier = "minecraft:bubble_column",
    states = listOf(CommonStates.dragDown),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 64, g = 64, b = 255, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightDampeningComponent(dampening = 1),
        ReplaceableComponent,
        MineableComponent(hardness = 100.0f)
    )
)
