package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.InternalFrictionComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent

object StructureVoid : BlockDefinition(
    identifier = "minecraft:structure_void",
    components = listOf(
        InternalFrictionComponent(internalFriction = 0.95f),
        MineableComponent(hardness = 0.0f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
