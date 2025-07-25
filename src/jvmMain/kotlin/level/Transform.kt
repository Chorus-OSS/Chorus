package org.chorus_oss.chorus.level

import org.chorus_oss.chorus.math.Rotator2
import org.chorus_oss.chorus.math.Vector3

class Transform : Locator {
    @JvmField
    var rotation: Rotator2

    @JvmField
    var headYaw: Double

    constructor(level: Level) : this(0.0, level)

    constructor(locator: Locator) : this(locator.position, Rotator2(0.0, 0.0), 0.0, locator.level)

    constructor(x: Double, level: Level) : this(x, 0.0, level)

    constructor(x: Double, y: Double, level: Level) : this(x, y, 0.0, level)

    constructor(x: Double, y: Double, z: Double, level: Level) : this(x, y, z, 0.0, level)

    constructor(x: Double, y: Double, z: Double, pitch: Double, level: Level) : this(x, y, z, pitch, 0.0, level)

    constructor(x: Double, y: Double, z: Double, pitch: Double, yaw: Double, level: Level) : this(
        x,
        y,
        z,
        pitch,
        yaw,
        0.0,
        level
    )

    constructor(x: Double, y: Double, z: Double, pitch: Double, yaw: Double, headYaw: Double, level: Level) : super(
        x,
        y,
        z,
        level
    ) {
        this.rotation = Rotator2(pitch, yaw)
        this.headYaw = headYaw
    }

    constructor(position: Vector3, rotation: Rotator2, headYaw: Double, level: Level) : super(
        position.x,
        position.y,
        position.z,
        level
    ) {
        this.rotation = rotation
        this.headYaw = headYaw
    }

    val yaw: Double
        get() = rotation.yaw

    fun setYaw(yaw: Double): Transform {
        rotation.yaw = yaw
        return this
    }

    val pitch: Double
        get() = rotation.pitch

    fun setPitch(pitch: Double): Transform {
        rotation.pitch = pitch
        return this
    }

    fun setHeadYaw(headYaw: Double): Transform {
        this.headYaw = headYaw
        return this
    }

    fun setX(x: Double): Transform {
        position.setX(x)
        return this
    }

    fun setY(y: Double): Transform {
        position.setY(y)
        return this
    }

    fun setZ(z: Double): Transform {
        position.setZ(z)
        return this
    }

    override fun toString(): String {
        return "Location (level=" + level.getLevelName() + ", x=" + position.x + ", y=" + position.y + ", z=" + position.z + ", yaw=" + rotation.yaw + ", pitch=" + rotation.pitch + ", headYaw=" + this.headYaw + ")"
    }

    fun add(x: Double): Transform {
        return this.add(x, 0.0, 0.0)
    }

    fun add(x: Double, y: Double): Transform {
        return this.add(x, y, 0.0)
    }

    override fun add(x: Double, y: Double, z: Double): Transform {
        return Transform(
            position.x + x,
            position.y + y,
            position.z + z,
            rotation.yaw, rotation.pitch, this.headYaw, this.level
        )
    }

    fun add(v: Vector3): Transform {
        return Transform(
            position.x + v.x,
            position.y + v.y,
            position.z + v.z,
            rotation.yaw, rotation.pitch, this.headYaw, this.level
        )
    }

    fun subtract(x: Double): Transform {
        return this.subtract(x, 0.0, 0.0)
    }

    fun subtract(x: Double, y: Double): Transform {
        return this.subtract(x, y, 0.0)
    }

    override fun subtract(x: Double, y: Double, z: Double): Transform {
        return this.add(-x, -y, -z)
    }

    fun subtract(v: Vector3): Transform {
        return this.add(-v.x, -v.y, -v.z)
    }

    fun multiply(number: Double): Transform {
        return Transform(
            position.x * number,
            position.y * number,
            position.z * number,
            rotation.yaw, rotation.pitch, this.headYaw, this.level
        )
    }

    fun divide(number: Double): Transform {
        return Transform(
            position.x / number,
            position.y / number,
            position.z / number,
            rotation.yaw, rotation.pitch, this.headYaw, this.level
        )
    }

    fun ceil(): Transform {
        return Transform(
            Math.ceil(position.x).toInt().toDouble(), Math.ceil(
                position.y
            ).toInt().toDouble(), Math.ceil(position.z).toInt().toDouble(),
            rotation.yaw, rotation.pitch, this.headYaw, this.level
        )
    }

    fun floor(): Transform {
        return Transform(
            position.floorX.toDouble(),
            position.floorY.toDouble(),
            position.floorZ.toDouble(),
            rotation.yaw, rotation.pitch, this.headYaw, this.level
        )
    }

    fun round(): Transform {
        return Transform(
            Math.round(position.x).toDouble(), Math.round(
                position.y
            ).toDouble(), Math.round(position.z).toDouble(),
            rotation.yaw, rotation.pitch, this.headYaw, this.level
        )
    }

    fun abs(): Transform {
        return Transform(
            Math.abs(position.x).toInt().toDouble(), Math.abs(
                position.y
            ).toInt().toDouble(), Math.abs(position.z).toInt().toDouble(),
            rotation.yaw, rotation.pitch, this.headYaw, this.level
        )
    }

    override fun clone(): Transform {
        return super.clone() as Transform
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun fromObject(
            pos: Vector3,
            level: Level,
            yaw: Double = 0.0,
            pitch: Double = 0.0,
            headYaw: Double = 0.0
        ): Transform {
            return Transform(pos.x, pos.y, pos.z, pitch, yaw, headYaw, level)
        }
    }
}
