package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.*

object LimeShulkerBox : BlockDefinition(
    identifier = "minecraft:lime_shulker_box",
    components = listOf(
        SolidComponent(solid = false),
        TransparentComponent(transparent = true),
        MapColorComponent(r = 127, g = 204, b = 25, a = 255),
        LightDampeningComponent(dampening = 1),
        MineableComponent(hardness = 2.0f),
        MoveableComponent(movement = MoveableComponent.Movement.Break, sticky = false)
    )
)
