package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.components.MapColorComponent
import org.chorus_oss.chorus.experimental.block.components.MineableComponent

object Jukebox : BlockDefinition(
    identifier = "minecraft:jukebox",
    components = listOf(MapColorComponent(r = 151, g = 109, b = 77, a = 255), MineableComponent(hardness = 1.0f))
)
