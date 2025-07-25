package org.chorus_oss.chorus.positiontracking

import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3

class PositionTracking(override var levelName: String, x: Double, y: Double, z: Double) :
    Vector3(x, y, z), NamedPosition {
    constructor(level: Level, x: Double, y: Double, z: Double) : this(level.getLevelName(), x, y, z)

    constructor(level: Level, v: Vector3) : this(level, v.x, v.y, v.z)

    constructor(levelName: String, v: Vector3) : this(levelName, v.x, v.y, v.z)

    constructor(pos: Locator) : this(pos.level, pos.position.x, pos.position.y, pos.position.z)

    constructor(pos: NamedPosition) : this(pos.levelName, pos.x, pos.y, pos.z)

    override fun add(x: Double): PositionTracking {
        return add(x, 0.0, 0.0)
    }

    override fun add(x: Double, y: Double): PositionTracking {
        return add(x, y, 0.0)
    }

    override fun add(x: Double, y: Double, z: Double): PositionTracking {
        return PositionTracking(levelName, this.x + x, this.y + y, this.z + z)
    }

    override fun add(v: Vector3): PositionTracking {
        return PositionTracking(levelName, x + v.x, y + v.y, z + v.z)
    }

    override fun subtract(x: Double): PositionTracking {
        return subtract(x, 0.0, 0.0)
    }

    override fun subtract(x: Double, y: Double): PositionTracking {
        return subtract(x, y, 0.0)
    }

    override fun subtract(x: Double, y: Double, z: Double): PositionTracking {
        return add(-x, -y, -z)
    }

    override fun subtract(v: Vector3): PositionTracking {
        return add(-v.x, -v.y, -v.z)
    }

    override fun multiply(number: Double): PositionTracking {
        return PositionTracking(levelName, x * number, y * number, z * number)
    }

    override fun divide(number: Double): PositionTracking {
        return PositionTracking(levelName, x * number, y * number, z * number)
    }

    override fun ceil(): PositionTracking {
        return PositionTracking(levelName, kotlin.math.ceil(x), kotlin.math.ceil(y), kotlin.math.ceil(z))
    }

    override fun floor(): PositionTracking {
        return PositionTracking(levelName, kotlin.math.floor(x), kotlin.math.floor(y), kotlin.math.floor(z))
    }

    override fun round(): PositionTracking {
        return PositionTracking(
            levelName, kotlin.math.round(this.x), kotlin.math.round(this.y), kotlin.math.round(this.z)
        )
    }

    override fun abs(): PositionTracking {
        return PositionTracking(
            levelName,
            kotlin.math.abs(this.x),
            kotlin.math.abs(this.y),
            kotlin.math.abs(this.z)
        )
    }

    override fun getSide(face: BlockFace): PositionTracking {
        return getSide(face, 1)
    }

    override fun getSide(face: BlockFace, step: Int): PositionTracking {
        return PositionTracking(
            levelName,
            x + face.xOffset * step,
            y + face.yOffset * step,
            z + face.zOffset * step
        )
    }

    override fun up(): PositionTracking {
        return up(1)
    }

    override fun up(step: Int): PositionTracking {
        return getSide(BlockFace.UP, step)
    }

    override fun down(): PositionTracking {
        return down(1)
    }

    override fun down(step: Int): PositionTracking {
        return getSide(BlockFace.DOWN, step)
    }

    override fun north(): PositionTracking {
        return north(1)
    }

    override fun north(step: Int): PositionTracking {
        return getSide(BlockFace.NORTH, step)
    }

    override fun south(): PositionTracking {
        return south(1)
    }

    override fun south(step: Int): PositionTracking {
        return getSide(BlockFace.SOUTH, step)
    }

    override fun east(): PositionTracking {
        return east(1)
    }

    override fun east(step: Int): PositionTracking {
        return getSide(BlockFace.EAST, step)
    }

    override fun west(): PositionTracking {
        return west(1)
    }

    override fun west(step: Int): PositionTracking {
        return getSide(BlockFace.WEST, step)
    }

    override fun getIntermediateWithXValue(v: Vector3, x: Double): PositionTracking? {
        val intermediateWithXValue = super.getIntermediateWithXValue(v, x) ?: return null
        return PositionTracking(levelName, intermediateWithXValue)
    }

    override fun getIntermediateWithYValue(v: Vector3, y: Double): Vector3? {
        val intermediateWithYValue = super.getIntermediateWithYValue(v, y) ?: return null
        return PositionTracking(levelName, intermediateWithYValue)
    }

    override fun getIntermediateWithZValue(v: Vector3, z: Double): Vector3? {
        val intermediateWithZValue = super.getIntermediateWithZValue(v, z) ?: return null
        return PositionTracking(levelName, intermediateWithZValue)
    }

    override fun setComponents(x: Double, y: Double, z: Double): Vector3 {
        super.setComponents(x, y, z)
        return this
    }

    override fun setComponents(position: Vector3): PositionTracking {
        super.setComponents(position)
        return this
    }

    override fun clone(): PositionTracking {
        return super.clone() as PositionTracking
    }
}
