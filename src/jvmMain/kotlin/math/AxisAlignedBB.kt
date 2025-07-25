package org.chorus_oss.chorus.math

import org.chorus_oss.chorus.level.MovingObjectPosition
import kotlin.math.floor

interface AxisAlignedBB : Cloneable {
    fun setBounds(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): AxisAlignedBB {
        this.minX = minX
        this.minY = minY
        this.minZ = minZ
        this.maxX = maxX
        this.maxY = maxY
        this.maxZ = maxZ
        return this
    }

    fun addCoord(x: Double, y: Double, z: Double): AxisAlignedBB {
        var minX = this.minX
        var minY = this.minY
        var minZ = this.minZ
        var maxX = this.maxX
        var maxY = this.maxY
        var maxZ = this.maxZ

        if (x < 0) minX += x
        if (x > 0) maxX += x

        if (y < 0) minY += y
        if (y > 0) maxY += y

        if (z < 0) minZ += z
        if (z > 0) maxZ += z

        return SimpleAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
    }

    fun grow(x: Double, y: Double, z: Double): AxisAlignedBB {
        return SimpleAxisAlignedBB(
            this.minX - x,
            minY - y,
            minZ - z,
            maxX + x,
            maxY + y,
            maxZ + z
        )
    }

    fun expand(x: Double, y: Double, z: Double): AxisAlignedBB {
        this.minX -= x
        this.minY -= y
        this.minZ -= z
        this.maxX += x
        this.maxY += y
        this.maxZ += z

        return this
    }

    fun offset(x: Double, y: Double, z: Double): AxisAlignedBB {
        this.minX += x
        this.minY += y
        this.minZ += z
        this.maxX += x
        this.maxY += y
        this.maxZ += z

        return this
    }

    fun shrink(x: Double, y: Double, z: Double): AxisAlignedBB {
        return SimpleAxisAlignedBB(
            this.minX + x,
            minY + y,
            minZ + z,
            maxX - x,
            maxY - y,
            maxZ - z
        )
    }

    fun contract(x: Double, y: Double, z: Double): AxisAlignedBB {
        this.minX += x
        this.minY += y
        this.minZ += z
        this.maxX -= x
        this.maxY -= y
        this.maxZ -= z

        return this
    }

    fun setBB(bb: AxisAlignedBB): AxisAlignedBB {
        this.minX = bb.minX
        this.minY = bb.minY
        this.minZ = bb.minZ
        this.maxX = bb.maxX
        this.maxY = bb.maxY
        this.maxZ = bb.maxZ
        return this
    }

    fun getOffsetBoundingBox(face: BlockFace, x: Double, y: Double, z: Double): AxisAlignedBB {
        return getOffsetBoundingBox(face.xOffset * x, face.yOffset * y, face.zOffset * z)
    }

    fun getOffsetBoundingBox(x: Double, y: Double, z: Double): AxisAlignedBB {
        return SimpleAxisAlignedBB(
            this.minX + x,
            minY + y,
            minZ + z,
            maxX + x,
            maxY + y,
            maxZ + z
        )
    }

    fun calculateXOffset(bb: AxisAlignedBB, x: Double): Double {
        var x3 = x
        if (bb.maxY <= this.minY || bb.minY >= this.maxY) {
            return x3
        }
        if (bb.maxZ <= this.minZ || bb.minZ >= this.maxZ) {
            return x3
        }
        if (x3 > 0 && bb.maxX <= this.minX) {
            val x1 = this.minX - bb.maxX
            if (x1 < x3) {
                x3 = x1
            }
        }
        if (x3 < 0 && bb.minX >= this.maxX) {
            val x2 = this.maxX - bb.minX
            if (x2 > x3) {
                x3 = x2
            }
        }

        return x3
    }

    fun calculateYOffset(bb: AxisAlignedBB, y: Double): Double {
        var y3 = y
        if (bb.maxX <= this.minX || bb.minX >= this.maxX) {
            return y3
        }
        if (bb.maxZ <= this.minZ || bb.minZ >= this.maxZ) {
            return y3
        }
        if (y3 > 0 && bb.maxY <= this.minY) {
            val y1 = this.minY - bb.maxY
            if (y1 < y3) {
                y3 = y1
            }
        }
        if (y3 < 0 && bb.minY >= this.maxY) {
            val y2 = this.maxY - bb.minY
            if (y2 > y3) {
                y3 = y2
            }
        }

        return y3
    }

    fun calculateZOffset(bb: AxisAlignedBB, z: Double): Double {
        var z3 = z
        if (bb.maxX <= this.minX || bb.minX >= this.maxX) {
            return z3
        }
        if (bb.maxY <= this.minY || bb.minY >= this.maxY) {
            return z3
        }
        if (z3 > 0 && bb.maxZ <= this.minZ) {
            val z1 = this.minZ - bb.maxZ
            if (z1 < z3) {
                z3 = z1
            }
        }
        if (z3 < 0 && bb.minZ >= this.maxZ) {
            val z2 = this.maxZ - bb.minZ
            if (z2 > z3) {
                z3 = z2
            }
        }

        return z3
    }

    fun intersectsWith(bb: AxisAlignedBB): Boolean {
        if (bb.maxY > this.minY && bb.minY < this.maxY) {
            if (bb.maxX > this.minX && bb.minX < this.maxX) {
                return bb.maxZ > this.minZ && bb.minZ < this.maxZ
            }
        }

        return false
    }

    fun isVectorInside(vector: Vector3): Boolean {
        return vector.x >= this.minX && vector.x <= this.maxX && vector.y >= this.minY && vector.y <= this.maxY && vector.z >= this.minZ && vector.z <= this.maxZ
    }

    fun isVectorInside(x: Double, y: Double, z: Double): Boolean {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ
    }

    val averageEdgeLength: Double
        get() = (this.maxX - this.minX + this.maxY - this.minY + this.maxZ - this.minZ) / 3

    fun isVectorInYZ(vector: Vector3): Boolean {
        return vector.y >= this.minY && vector.y <= this.maxY && vector.z >= this.minZ && vector.z <= this.maxZ
    }

    fun isVectorInXZ(vector: Vector3): Boolean {
        return vector.x >= this.minX && vector.x <= this.maxX && vector.z >= this.minZ && vector.z <= this.maxZ
    }

    fun isVectorInXY(vector: Vector3): Boolean {
        return vector.x >= this.minX && vector.x <= this.maxX && vector.y >= this.minY && vector.y <= this.maxY
    }

    fun calculateIntercept(pos1: Vector3, pos2: Vector3): MovingObjectPosition? {
        var v1 = pos1.getIntermediateWithXValue(pos2, this.minX)
        var v2 = pos1.getIntermediateWithXValue(pos2, this.maxX)
        var v3 = pos1.getIntermediateWithYValue(pos2, this.minY)
        var v4 = pos1.getIntermediateWithYValue(pos2, this.maxY)
        var v5 = pos1.getIntermediateWithZValue(pos2, this.minZ)
        var v6 = pos1.getIntermediateWithZValue(pos2, this.maxZ)

        if (v1 != null && !this.isVectorInYZ(v1)) {
            v1 = null
        }

        if (v2 != null && !this.isVectorInYZ(v2)) {
            v2 = null
        }

        if (v3 != null && !this.isVectorInXZ(v3)) {
            v3 = null
        }

        if (v4 != null && !this.isVectorInXZ(v4)) {
            v4 = null
        }

        if (v5 != null && !this.isVectorInXY(v5)) {
            v5 = null
        }

        if (v6 != null && !this.isVectorInXY(v6)) {
            v6 = null
        }

        var vector: Vector3? = null

        //if (v1 != null && (vector == null || pos1.distanceSquared(v1) < pos1.distanceSquared(vector))) {
        if (v1 != null) {
            vector = v1
        }

        if (v2 != null && (vector == null || pos1.distanceSquared(v2) < pos1.distanceSquared(vector))) {
            vector = v2
        }

        if (v3 != null && (vector == null || pos1.distanceSquared(v3) < pos1.distanceSquared(vector))) {
            vector = v3
        }

        if (v4 != null && (vector == null || pos1.distanceSquared(v4) < pos1.distanceSquared(vector))) {
            vector = v4
        }

        if (v5 != null && (vector == null || pos1.distanceSquared(v5) < pos1.distanceSquared(vector))) {
            vector = v5
        }

        if (v6 != null && (vector == null || pos1.distanceSquared(v6) < pos1.distanceSquared(vector))) {
            vector = v6
        }

        if (vector == null) {
            return null
        }

        val f: BlockFace = if (vector === v1) {
            BlockFace.WEST
        } else if (vector === v2) {
            BlockFace.EAST
        } else if (vector === v3) {
            BlockFace.DOWN
        } else if (vector === v4) {
            BlockFace.UP
        } else if (vector === v5) {
            BlockFace.NORTH
        } else {
            BlockFace.SOUTH
        }

        return MovingObjectPosition.fromBlock(0, 0, 0, f, vector)
    }

    var minX: Double

    var minY: Double

    var minZ: Double

    var maxX: Double

    var maxY: Double

    var maxZ: Double

    public override fun clone(): AxisAlignedBB

    fun forEach(action: BBConsumer<*>) {
        val minX = floor(this.minX).toInt()
        val minY = floor(this.minY).toInt()
        val minZ = floor(this.minZ).toInt()

        val maxX = floor(this.maxX).toInt()
        val maxY = floor(this.maxY).toInt()
        val maxZ = floor(this.maxZ).toInt()

        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    action.accept(x, y, z)
                }
            }
        }
    }

    interface BBConsumer<T> {
        fun accept(x: Int, y: Int, z: Int)

        fun get(): T? {
            return null
        }
    }

    companion object {
        val EMPTY_ARRAY: Array<AxisAlignedBB?> = arrayOfNulls(0)

        val EMPTY_LIST: List<AxisAlignedBB> = listOf()
    }
}
