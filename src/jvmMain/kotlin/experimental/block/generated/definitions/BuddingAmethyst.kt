package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent

object BuddingAmethyst : BlockDefinition(
    identifier = "minecraft:budding_amethyst",
    components = listOf(
        MapColorComponent(r = 153, g = 90, b = 205, a = 255),
        MineableComponent(hardness = 1.5f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
