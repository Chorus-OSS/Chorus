package org.chorus_oss.chorus.level

import org.chorus_oss.chorus.math.Vector3
import kotlin.math.floor

class ChunkPosition(val x: Int, val y: Int, val z: Int) {
    constructor(vec3d: Vector3) : this(floor(vec3d.x).toInt(), floor(vec3d.y).toInt(), floor(vec3d.z).toInt())

    override fun equals(other: Any?): Boolean {
        return if (other !is ChunkPosition) {
            false
        } else {
            other.x == this.x && other.y == this.y && other.z == this.z
        }
    }

    override fun hashCode(): Int {
        return this.x * 8976890 + this.y * 981131 + this.z
    }
}
