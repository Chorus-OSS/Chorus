package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object GlowLichen : BlockDefinition(
    identifier = "minecraft:glow_lichen",
    states = listOf(CommonStates.multiFaceDirectionBits),
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 127, g = 167, b = 150, a = 255),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightEmissionComponent(emission = 7),
        LightDampeningComponent(dampening = 1),
        ReplaceableComponent,
        MineableComponent(hardness = 0.2f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
