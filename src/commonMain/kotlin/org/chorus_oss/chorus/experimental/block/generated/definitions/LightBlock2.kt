package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object LightBlock2 : BlockDefinition(
    identifier = "minecraft:light_block_2",
    components = listOf(
        TransparentComponent(transparent = true),
        InternalFrictionComponent(internalFriction = 0.95f),
        LightEmissionComponent(emission = 2),
        LightDampeningComponent(dampening = 1),
        ReplaceableComponent,
        MineableComponent(hardness = 0.0f),
        CollisionBoxComponent(enabled = false)
    )
)
