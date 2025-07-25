package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent

object MelonBlock : BlockDefinition(
    identifier = "minecraft:melon_block",
    components = listOf(
        MapColorComponent(r = 127, g = 204, b = 25, a = 255),
        MineableComponent(hardness = 1.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
