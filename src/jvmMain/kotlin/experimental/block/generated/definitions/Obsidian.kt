package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent

object Obsidian : BlockDefinition(
    identifier = "minecraft:obsidian",
    components = listOf(
        MapColorComponent(r = 25, g = 25, b = 25, a = 255),
        MineableComponent(hardness = 35.0f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
