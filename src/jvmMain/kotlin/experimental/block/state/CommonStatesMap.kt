package org.chorus_oss.chorus.experimental.block.state

import org.chorus_oss.chorus.block.property.enums.MinecraftCardinalDirection
import org.chorus_oss.chorus.math.BlockFace

@Suppress("unused")
object CommonStatesMap {
    val ewsnDirection = mapOf(
        BlockFace.EAST to 0,
        BlockFace.WEST to 1,
        BlockFace.SOUTH to 2,
        BlockFace.NORTH to 3,
    )

    val cardinalBlockface = mapOf(
        MinecraftCardinalDirection.EAST to BlockFace.EAST,
        MinecraftCardinalDirection.WEST to BlockFace.WEST,
        MinecraftCardinalDirection.SOUTH to BlockFace.SOUTH,
        MinecraftCardinalDirection.NORTH to BlockFace.NORTH,
    )
}