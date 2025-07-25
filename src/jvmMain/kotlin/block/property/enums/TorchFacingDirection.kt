package org.chorus_oss.chorus.block.property.enums

import org.chorus_oss.chorus.math.BlockFace

enum class TorchFacingDirection(val torchDirection: BlockFace? = null) {
    UNKNOWN(null),
    WEST(BlockFace.EAST),
    EAST(BlockFace.WEST),
    NORTH(BlockFace.SOUTH),
    SOUTH(BlockFace.NORTH),
    TOP(BlockFace.UP);

    val attachedFace: BlockFace
        /**
         * The direction that is touching the attached block.
         */
        get() = when (this) {
            EAST -> BlockFace.EAST
            WEST -> BlockFace.WEST
            SOUTH -> BlockFace.SOUTH
            NORTH -> BlockFace.NORTH
            else -> BlockFace.DOWN
        }

    companion object {
        fun getByTorchDirection(face: BlockFace): TorchFacingDirection {
            return when (face) {
                BlockFace.UP -> TOP
                BlockFace.EAST -> WEST
                BlockFace.WEST -> EAST
                BlockFace.SOUTH -> NORTH
                BlockFace.NORTH -> SOUTH
                else -> UNKNOWN
            }
        }

        fun getByAttachedFace(face: BlockFace): TorchFacingDirection {
            return when (face) {
                BlockFace.DOWN -> TOP
                BlockFace.SOUTH -> SOUTH
                BlockFace.NORTH -> NORTH
                BlockFace.EAST -> EAST
                BlockFace.WEST -> WEST
                else -> UNKNOWN
            }
        }
    }
}
