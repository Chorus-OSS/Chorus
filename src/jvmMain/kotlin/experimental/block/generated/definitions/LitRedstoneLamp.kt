package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object LitRedstoneLamp : BlockDefinition(
    identifier = "minecraft:lit_redstone_lamp",
    components = listOf(LightEmissionComponent(emission = 15), MineableComponent(hardness = 0.3f))
)
