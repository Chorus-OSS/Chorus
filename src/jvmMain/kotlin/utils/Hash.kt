package org.chorus_oss.chorus.utils

import org.chorus_oss.chorus.math.Vector3

object Hash {
    fun hashBlock(x: Int, y: Int, z: Int): Long {
        return (y.toLong() shl 52) + ((z.toLong() and 0x3ffffffL) shl 26) + (x.toLong() and 0x3ffffffL)
    }

    fun hashBlockX(triple: Long): Int {
        return (((triple and 0x3ffffffL) shl 38) shr 38).toInt()
    }

    fun hashBlockY(triple: Long): Int {
        return (triple shr 52).toInt()
    }

    fun hashBlockZ(triple: Long): Int {
        return ((((triple shr 26) and 0x3ffffffL) shl 38) shr 38).toInt()
    }

    @JvmStatic
    fun hashBlock(blockPos: Vector3): Long {
        return hashBlock(blockPos.floorX, blockPos.floorY, blockPos.floorZ)
    }
}
