package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object StructureBlock : BlockDefinition(
    identifier = "minecraft:structure_block",
    states = listOf(CommonStates.structureBlockType),
    components = listOf(
        MapColorComponent(r = 153, g = 153, b = 153, a = 255),
        MineableComponent(hardness = -1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
