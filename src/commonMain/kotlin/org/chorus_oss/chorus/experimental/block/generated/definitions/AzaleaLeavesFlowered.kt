package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object AzaleaLeavesFlowered : BlockDefinition(
    identifier = "minecraft:azalea_leaves_flowered",
    states = listOf(CommonStates.persistentBit, CommonStates.updateBit),
    components = listOf(
        TransparentComponent(transparent = true),
        MapColorComponent(r = 0, g = 124, b = 0, a = 255),
        LightDampeningComponent(dampening = 1),
        FlammableComponent(catchChance = 30, destroyChance = 60),
        MineableComponent(hardness = 0.2f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
