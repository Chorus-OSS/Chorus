package org.chorus_oss.chorus.level

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3


class MovingObjectPosition {
    /**
     * 0 = block, 1 = entity
     */
    var typeOfHit: Int = 0

    var blockX: Int = 0
    var blockY: Int = 0
    var blockZ: Int = 0

    /**
     * Which side was hit. If its -1 then it went the full length of the ray trace.
     * Bottom = 0, Top = 1, East = 2, West = 3, North = 4, South = 5.
     */
    var sideHit: Int = 0

    @JvmField
    var hitVector: Vector3? = null

    var entityHit: Entity? = null


    var faceHit: BlockFace?
        get() = when (sideHit) {
            0 -> BlockFace.DOWN
            1 -> BlockFace.UP
            2 -> BlockFace.EAST
            3 -> BlockFace.WEST
            4 -> BlockFace.NORTH
            5 -> BlockFace.SOUTH
            else -> null
        }
        set(face) {
            sideHit = when (face) {
                BlockFace.DOWN -> 0
                BlockFace.UP -> 1
                BlockFace.NORTH -> 4
                BlockFace.SOUTH -> 5
                BlockFace.WEST -> 3
                BlockFace.EAST -> 2
                null -> -1
            }
        }

    override fun toString(): String {
        return "MovingObjectPosition{" +
                "typeOfHit=" + typeOfHit +
                ", blockX=" + blockX +
                ", blockY=" + blockY +
                ", blockZ=" + blockZ +
                ", sideHit=" + sideHit +
                ", hitVector=" + hitVector +
                ", entityHit=" + entityHit +
                '}'
    }

    companion object {
        fun fromBlock(x: Int, y: Int, z: Int, face: BlockFace?, hitVector: Vector3): MovingObjectPosition {
            val objectPosition = MovingObjectPosition()
            objectPosition.typeOfHit = 0
            objectPosition.blockX = x
            objectPosition.blockY = y
            objectPosition.blockZ = z
            objectPosition.hitVector = Vector3(hitVector.x, hitVector.y, hitVector.z)
            objectPosition.faceHit = face
            return objectPosition
        }


        fun fromEntity(entity: Entity): MovingObjectPosition {
            val objectPosition = MovingObjectPosition()
            objectPosition.typeOfHit = 1
            objectPosition.entityHit = entity
            objectPosition.hitVector = Vector3(entity.position.x, entity.position.y, entity.position.z)
            return objectPosition
        }
    }
}
