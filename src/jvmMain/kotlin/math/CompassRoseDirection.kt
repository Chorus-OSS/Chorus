package org.chorus_oss.chorus.math


/**
 * Represents a 16 direction compass rose.
 *
 * https://en.wikipedia.org/wiki/Compass_rose#/media/File:Brosen_windrose.svg
 */
enum class CompassRoseDirection {
    NORTH(0, -1, BlockFace.NORTH, 0.0, 8),
    EAST(1, 0, BlockFace.EAST, 90.0, 12),
    SOUTH(0, 1, BlockFace.SOUTH, 180.0, 0),
    WEST(-1, 0, BlockFace.WEST, 270.0, 4),
    NORTH_EAST(NORTH, EAST, BlockFace.NORTH, 45.0, 10),
    NORTH_WEST(NORTH, WEST, BlockFace.WEST, 315.0, 6),
    SOUTH_EAST(SOUTH, EAST, BlockFace.EAST, 135.0, 14),
    SOUTH_WEST(SOUTH, WEST, BlockFace.SOUTH, 225.0, 2),
    WEST_NORTH_WEST(WEST, NORTH_WEST, BlockFace.WEST, 292.5, 5),
    NORTH_NORTH_WEST(NORTH, NORTH_WEST, BlockFace.NORTH, 337.5, 7),
    NORTH_NORTH_EAST(NORTH, NORTH_EAST, BlockFace.NORTH, 22.5, 9),
    EAST_NORTH_EAST(EAST, NORTH_EAST, BlockFace.EAST, 67.5, 11),
    EAST_SOUTH_EAST(EAST, SOUTH_EAST, BlockFace.EAST, 112.5, 13),
    SOUTH_SOUTH_EAST(SOUTH, SOUTH_EAST, BlockFace.SOUTH, 157.5, 15),
    SOUTH_SOUTH_WEST(SOUTH, SOUTH_WEST, BlockFace.SOUTH, 202.5, 1),
    WEST_SOUTH_WEST(WEST, SOUTH_WEST, BlockFace.WEST, 247.5, 3);

    /**
     * Get the amount of X-coordinates to modify to get the represented block
     *
     * @return Amount of X-coordinates to modify
     */
    val modX: Int

    /**
     * Get the amount of Z-coordinates to modify to get the represented block
     *
     * @return Amount of Z-coordinates to modify
     */
    val modZ: Int

    /**
     * Gets the closest face for this direction. For example, NNE returns N.
     * Even directions like NE will return the direction to the left, N in this case.
     */
    @JvmField
    val closestBlockFace: BlockFace

    /**
     * Gets the [org.chorus_oss.chorus.entity.Entity] yaw that represents this direction.
     *
     * @return The yaw value that can be used by entities to look at this direction.
     * @since 1.4.0.0-PN
     */
    val yaw: Float

    @JvmField
    val index: Int

    constructor(modX: Int, modZ: Int, closestBlockFace: BlockFace, yaw: Double, index: Int) {
        this.modX = modX
        this.modZ = modZ
        this.closestBlockFace = closestBlockFace
        this.yaw = yaw.toFloat()
        this.index = index
    }

    constructor(
        face1: CompassRoseDirection,
        face2: CompassRoseDirection,
        closestBlockFace: BlockFace,
        yaw: Double,
        index: Int
    ) {
        this.modX = face1.modX + face2.modX
        this.modZ = face1.modZ + face2.modZ
        this.closestBlockFace = closestBlockFace
        this.yaw = yaw.toFloat()
        this.index = index
    }

    val oppositeFace: CompassRoseDirection
        get() = when (this) {
            NORTH -> SOUTH
            SOUTH -> NORTH
            EAST -> WEST
            WEST -> EAST
            NORTH_EAST -> SOUTH_WEST
            NORTH_WEST -> SOUTH_EAST
            SOUTH_EAST -> NORTH_WEST
            SOUTH_WEST -> NORTH_EAST
            WEST_NORTH_WEST -> EAST_SOUTH_EAST
            NORTH_NORTH_WEST -> SOUTH_SOUTH_EAST
            NORTH_NORTH_EAST -> SOUTH_SOUTH_WEST
            EAST_NORTH_EAST -> WEST_SOUTH_WEST
            EAST_SOUTH_EAST -> WEST_NORTH_WEST
            SOUTH_SOUTH_EAST -> NORTH_NORTH_WEST
            SOUTH_SOUTH_WEST -> NORTH_NORTH_EAST
            WEST_SOUTH_WEST -> EAST_NORTH_EAST
            else -> throw IncompatibleClassChangeError("New values was added to the enum")
        }

    enum class Precision(val directions: Int) {
        /**
         * North, South, East, West.
         */
        CARDINAL(4),

        /**
         * N, E, S, W, NE, NW, SE, SW.
         */
        PRIMARY_INTER_CARDINAL(8),

        /**
         * N, E, S, W, NE, NW, SE, SW, WNW, NNW, NNE, ENE, ESE, SSE, SSW, WSW.
         */
        SECONDARY_INTER_CARDINAL(16);
    }

    companion object {
        private val VALUES = arrayOf(
            SOUTH, SOUTH_SOUTH_WEST, SOUTH_WEST, WEST_SOUTH_WEST,
            WEST, WEST_NORTH_WEST, NORTH_WEST, NORTH_NORTH_WEST,
            NORTH, NORTH_NORTH_EAST, NORTH_EAST, EAST_NORTH_EAST,
            EAST, EAST_SOUTH_EAST, SOUTH_EAST, SOUTH_SOUTH_EAST
        )

        /**
         * Gets the closes direction based on the given [org.chorus_oss.chorus.entity.Entity] yaw.
         *
         * @param yaw An entity yaw
         * @return The closest direction
         * @since 1.4.0.0-PN
         */
        fun getClosestFromYaw(yaw: Double, precision: Precision): CompassRoseDirection {
            return from(
                Math.round(Math.round((yaw + 180.0) * precision.directions / 360.0) * (16.0 / precision.directions))
                    .toInt() and 0x0f
            )
        }

        /**
         * Gets the closes direction based on the given [org.chorus_oss.chorus.entity.Entity] yaw.
         *
         * @param yaw An entity yaw
         * @return The closest direction
         * @since 1.4.0.0-PN
         */
        fun getClosestFromYaw(yaw: Double): CompassRoseDirection {
            return getClosestFromYaw(yaw, Precision.SECONDARY_INTER_CARDINAL)
        }

        @JvmStatic
        fun from(index: Int): CompassRoseDirection {
            return VALUES[index]
        }
    }
}
