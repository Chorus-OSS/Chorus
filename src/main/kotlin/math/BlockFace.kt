package org.chorus_oss.chorus.math

import org.chorus_oss.chorus.utils.ChorusRandom
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Predicate
import kotlin.math.abs
import kotlin.math.floor

enum class BlockFace(
    /**
     * Ordering index for D-U-N-S-W-E
     */
    val index: Int,
    /**
     * Index of the opposite BlockFace in the VALUES array
     */
    private val opposite: Int,
    /**
     * Ordering index for the HORIZONTALS field (S-W-N-E)
     */
    val horizontalIndex: Int,
    /**
     * The name of this BlockFace (up, down, north, etc.)
     */
    val faceName: String,
    val axis: Axis,
    val axisDirection: AxisDirection,
    /**
     * Normalized vector that points in the direction of this BlockFace
     */
    val unitVector: Vector3
) {
    DOWN(0, 1, -1, "down", Axis.Y, AxisDirection.NEGATIVE, Vector3(0.0, -1.0, 0.0)),
    UP(1, 0, -1, "up", Axis.Y, AxisDirection.POSITIVE, Vector3(0.0, 1.0, 0.0)),
    NORTH(2, 3, 2, "north", Axis.Z, AxisDirection.NEGATIVE, Vector3(0.0, 0.0, -1.0)),
    SOUTH(3, 2, 0, "south", Axis.Z, AxisDirection.POSITIVE, Vector3(0.0, 0.0, 1.0)),
    WEST(4, 5, 1, "west", Axis.X, AxisDirection.NEGATIVE, Vector3(-1.0, 0.0, 0.0)),
    EAST(5, 4, 3, "east", Axis.X, AxisDirection.POSITIVE, Vector3(1.0, 0.0, 0.0));

    /**
     * Get the unit vector of this BlockFace
     *
     * @return vector
     */

    val indexDUNSEW: Int
        /**
         * Get the index of this BlockFace (0-5). The order is D-U-N-E-S-W
         *
         * @return index
         */
        get() = when (index) {
            1 -> 1
            2 -> 2
            3 -> 4
            4 -> 5
            5 -> 3
            else -> 0
        }

    val indexDUSWNE: Int
        /**
         * Get the index of this BlockFace (0-5). The order is D-U-S-W-N-E
         *
         * @return index
         */
        get() = when (index) {
            1 -> 1
            2 -> 4
            3 -> 2
            4 -> 3
            5 -> 5
            else -> 0
        }

    val horizontalAngle: Float
        /**
         * Get the angle of this BlockFace (0-360)
         *
         * @return horizontal angle
         */
        get() = ((horizontalIndex and 3) * 90).toFloat()

    val xOffset: Int
        /**
         * Returns an offset that addresses the block in front of this BlockFace
         *
         * @return x offset
         */
        get() = if (axis == Axis.X) axisDirection.offset else 0

    val yOffset: Int
        /**
         * Returns an offset that addresses the block in front of this BlockFace
         *
         * @return y offset
         */
        get() = if (axis == Axis.Y) axisDirection.offset else 0

    val zOffset: Int
        /**
         * Returns an offset that addresses the block in front of this BlockFace
         *
         * @return x offset
         */
        get() = if (axis == Axis.Z) axisDirection.offset else 0

    /**
     * Get the opposite BlockFace (e.g. DOWN ==&gt; UP)
     *
     * @return block face
     */
    fun getOpposite(): BlockFace {
        return fromIndex(opposite)
    }

    /**
     * Rotate this BlockFace around the Y axis clockwise (NORTH =&gt; EAST =&gt; SOUTH =&gt; WEST =&gt; NORTH)
     *
     * @return block face
     */
    fun rotateY(): BlockFace {
        return when (this) {
            NORTH -> EAST
            EAST -> SOUTH
            SOUTH -> WEST
            WEST -> NORTH
            else -> throw RuntimeException("Unable to get Y-rotated face of $this")
        }
    }

    /**
     * Rotate this BlockFace around the Y axis counter-clockwise (NORTH =&gt; WEST =&gt; SOUTH =&gt; EAST =&gt; NORTH)
     *
     * @return block face
     */
    fun rotateYCCW(): BlockFace {
        return when (this) {
            NORTH -> WEST
            EAST -> NORTH
            SOUTH -> EAST
            WEST -> SOUTH
            else -> throw RuntimeException("Unable to get counter-clockwise Y-rotated face of $this")
        }
    }

    val compassRoseDirection: CompassRoseDirection?
        get() = when (this) {
            NORTH -> CompassRoseDirection.NORTH
            SOUTH -> CompassRoseDirection.SOUTH
            WEST -> CompassRoseDirection.WEST
            EAST -> CompassRoseDirection.EAST
            else -> null
        }

    val edges: Set<BlockFace>
        get() {
            val blockFaces = EnumSet.noneOf(BlockFace::class.java)
            if (axis.isVertical) {
                Collections.addAll(blockFaces, *Plane.HORIZONTAL_FACES.toTypedArray())
                return blockFaces
            }
            Collections.addAll(blockFaces, *Plane.VERTICAL_FACES.toTypedArray())
            val edgeAxis =
                if (axis == Axis.X) Axis.Z else Axis.X
            blockFaces.add(fromAxis(AxisDirection.NEGATIVE, edgeAxis))
            blockFaces.add(fromAxis(AxisDirection.POSITIVE, edgeAxis))
            return blockFaces
        }

    override fun toString(): String {
        return faceName
    }

    enum class Axis(val axisName: String) : Predicate<BlockFace?> {
        Y("y"),
        X("x"),
        Z("z");

        var plane: Plane? = null
            private set

        val isVertical: Boolean
            get() = plane == Plane.VERTICAL

        val isHorizontal: Boolean
            get() = plane == Plane.HORIZONTAL

        override fun test(face: BlockFace?): Boolean {
            return face != null && face.axis == this
        }

        override fun toString(): String {
            return axisName
        }

        companion object {
            init {
                //Circular dependency
                X.plane = Plane.HORIZONTAL
                Y.plane = Plane.VERTICAL
                Z.plane = Plane.HORIZONTAL
            }
        }
    }

    enum class AxisDirection(val offset: Int, private val description: String) {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        override fun toString(): String {
            return description
        }
    }

    enum class Plane(val faces: Array<BlockFace>) : Predicate<BlockFace?> {
        HORIZONTAL(arrayOf(NORTH, EAST, SOUTH, WEST)),
        VERTICAL(arrayOf(UP, DOWN));

        fun random(): BlockFace {
            return faces[ThreadLocalRandom.current().nextInt(faces.size - 1)]
        }

        fun random(rand: ChorusRandom): BlockFace {
            return faces[rand.nextInt(faces.size - 1)]
        }

        override fun test(face: BlockFace?): Boolean {
            return face != null && face.axis.plane == this
        }

        companion object {
            val HORIZONTAL_FACES
                get() = listOf(
                    NORTH,
                    EAST,
                    SOUTH,
                    WEST,
                )

            val VERTICAL_FACES
                get() = listOf(
                    UP,
                    DOWN,
                )
        }
    }

    companion object {
        /**
         * All faces in D-U-N-S-W-E order
         */
        private val VALUES = Array(entries.maxOf { it.index } + 1) { index -> entries.single { it.index == index } }

        /**
         * All faces with horizontal axis in order S-W-N-E
         */
        private val HORIZONTALS =
            Array(entries.count { it.axis.isHorizontal }) { index -> entries.single { it.axis.isHorizontal && it.horizontalIndex == index } }

        init {
            for (face in entries) {
                VALUES[face.index] = face

                if (face.axis.isHorizontal) {
                    HORIZONTALS[face.horizontalIndex] = face
                }
            }
        }

        @JvmStatic
        val horizontals: Array<BlockFace>
            get() = HORIZONTALS.clone()

        /**
         * Get a BlockFace by its index (0-5). The order is D-U-N-S-W-E
         *
         * @param index BlockFace index
         * @return block face
         */
        @JvmStatic
        fun fromIndex(index: Int): BlockFace {
            return VALUES[abs(index % VALUES.size)]
        }

        /**
         * Get a BlockFace by its horizontal index (0-3). The order is S-W-N-E
         *
         * @param index BlockFace index
         * @return block face
         */
        @JvmStatic
        fun fromHorizontalIndex(index: Int): BlockFace {
            return HORIZONTALS[abs(index % HORIZONTALS.size)]
        }

        /**
         * Get the BlockFace corresponding to the given angle (0-360). An angle of 0 is SOUTH, an angle of 90 would be WEST
         *
         * @param angle horizontal angle
         * @return block face
         */
        fun fromHorizontalAngle(angle: Double): BlockFace {
            return fromHorizontalIndex(floor(angle / 90.0 + 0.5).toInt() and 3)
        }

        fun fromAxis(axisDirection: AxisDirection, axis: Axis): BlockFace {
            for (face in VALUES) {
                if (face.axisDirection == axisDirection && face.axis == axis) {
                    return face
                }
            }

            throw RuntimeException("Unable to get face from axis: $axisDirection $axis")
        }

        /**
         * Choose a random BlockFace using the given Random
         *
         * @param rand random number generator
         * @return block face
         */
        @JvmStatic
        fun random(rand: Random): BlockFace {
            return VALUES[rand.nextInt(VALUES.size)]
        }
    }
}
