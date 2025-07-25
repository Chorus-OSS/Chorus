package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent

object Barrier : BlockDefinition(
    identifier = "minecraft:barrier",
    components = listOf(
        MineableComponent(hardness = -1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
