package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent
import org.chorus_oss.chorus.experimental.block.components.MoveableComponent

object Glowingobsidian : BlockDefinition(
    identifier = "minecraft:glowingobsidian",
    components = listOf(
        MapColorComponent(r = 25, g = 25, b = 25, a = 255),
        LightEmissionComponent(emission = 12),
        MineableComponent(hardness = 50.0f),
        MoveableComponent(movement = MoveableComponent.Movement.None, sticky = false)
    )
)
