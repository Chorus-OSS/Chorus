package org.chorus_oss.chorus.block.property.enums

import org.chorus_oss.chorus.math.BlockFace

enum class LeverDirection(val metadata: Int, val directionName: String, val facing: BlockFace) {
    DOWN_EAST_WEST(0, "down_x", BlockFace.DOWN),

    DOWN_NORTH_SOUTH(7, "down_z", BlockFace.DOWN),

    EAST(1, "east", BlockFace.EAST),

    NORTH(4, "north", BlockFace.NORTH),

    SOUTH(3, "south", BlockFace.SOUTH),

    UP_EAST_WEST(6, "up_x", BlockFace.UP),

    UP_NORTH_SOUTH(5, "up_z", BlockFace.UP),

    WEST(2, "west", BlockFace.WEST);


    override fun toString(): String {
        return this.directionName
    }

    companion object {
        private val META_LOOKUP = arrayOfNulls<LeverDirection>(entries.toTypedArray().size)
        fun byMetadata(meta: Int): LeverDirection? {
            var meta = meta
            if (meta < 0 || meta >= META_LOOKUP.size) {
                meta = 0
            }
            return META_LOOKUP[meta]
        }

        fun forFacings(clickedSide: BlockFace, playerDirection: BlockFace): LeverDirection {
            return when (clickedSide) {
                BlockFace.DOWN -> when (playerDirection.axis) {
                    BlockFace.Axis.X -> DOWN_EAST_WEST
                    BlockFace.Axis.Z -> DOWN_NORTH_SOUTH
                    else -> throw IllegalArgumentException("Invalid entityFacing $playerDirection for facing $clickedSide")
                }

                BlockFace.UP -> when (playerDirection.axis) {
                    BlockFace.Axis.X -> UP_EAST_WEST
                    BlockFace.Axis.Z -> UP_NORTH_SOUTH
                    else -> throw IllegalArgumentException("Invalid entityFacing $playerDirection for facing $clickedSide")
                }

                BlockFace.NORTH -> NORTH
                BlockFace.SOUTH -> SOUTH
                BlockFace.WEST -> WEST
                BlockFace.EAST -> EAST
            }
        }

        init {
            for (face in entries) {
                META_LOOKUP[face.metadata] = face
            }
        }
    }
}
