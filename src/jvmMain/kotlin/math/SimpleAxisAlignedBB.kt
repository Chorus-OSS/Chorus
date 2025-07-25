package org.chorus_oss.chorus.math

import kotlin.math.max
import kotlin.math.min


class SimpleAxisAlignedBB : AxisAlignedBB {
    override var minX: Double
    override var minY: Double
    override var minZ: Double
    override var maxX: Double
    override var maxY: Double
    override var maxZ: Double

    constructor(pos1: Vector3, pos2: Vector3) {
        this.minX = min(pos1.x, pos2.x)
        this.minY = min(pos1.y, pos2.y)
        this.minZ = min(pos1.z, pos2.z)
        this.maxX = max(pos1.x, pos2.x)
        this.maxY = max(pos1.y, pos2.y)
        this.maxZ = max(pos1.z, pos2.z)
    }

    constructor(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) {
        this.minX = minX
        this.minY = minY
        this.minZ = minZ
        this.maxX = maxX
        this.maxY = maxY
        this.maxZ = maxZ
    }

    override fun toString(): String {
        return "AxisAlignedBB(" + this.minX + ", " + this.minY + ", " + this.minZ + ", " + this.maxX + ", " + this.maxY + ", " + this.maxZ + ")"
    }

    override fun clone(): AxisAlignedBB {
        return SimpleAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
    }
}
