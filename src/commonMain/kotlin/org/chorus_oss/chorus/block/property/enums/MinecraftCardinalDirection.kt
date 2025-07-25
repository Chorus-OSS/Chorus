package org.chorus_oss.chorus.block.property.enums

enum class MinecraftCardinalDirection {
    SOUTH,
    WEST,
    NORTH,
    EAST;

    companion object {
        val VALUES: Array<MinecraftCardinalDirection> = entries.toTypedArray()
    }
}
