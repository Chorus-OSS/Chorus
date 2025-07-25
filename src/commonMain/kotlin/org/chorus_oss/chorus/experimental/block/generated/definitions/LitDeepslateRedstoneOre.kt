package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.LightEmissionComponent
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object LitDeepslateRedstoneOre : BlockDefinition(
    identifier = "minecraft:lit_deepslate_redstone_ore",
    components = listOf(
        MapColorComponent(r = 100, g = 100, b = 100, a = 255),
        LightEmissionComponent(emission = 9),
        MineableComponent(hardness = 4.5f)
    )
)
