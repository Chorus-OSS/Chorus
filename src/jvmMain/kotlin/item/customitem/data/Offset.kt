package org.chorus_oss.chorus.item.customitem.data

import org.chorus_oss.chorus.math.Vector3f

/**
 * Offset代表RenderOffsets中的偏移量对象
 *
 *
 * This represents the offset object in RenderOffsets
 */
class Offset private constructor() {
    var position: Vector3f? = null
        private set
    var rotation: Vector3f? = null
        private set
    var scale: Vector3f? = null
        private set

    fun position(x: Float, y: Float, z: Float): Offset {
        this.position = Vector3f(x, y, z)
        return this
    }

    fun rotation(x: Float, y: Float, z: Float): Offset {
        this.rotation = Vector3f(x, y, z)
        return this
    }

    fun scale(x: Float, y: Float, z: Float): Offset {
        this.scale = Vector3f(x, y, z)
        return this
    }

    companion object {
        fun builder(): Offset {
            return Offset()
        }
    }
}
