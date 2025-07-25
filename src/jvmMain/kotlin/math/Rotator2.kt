package org.chorus_oss.chorus.math


import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class Rotator2 @JvmOverloads constructor(@JvmField var pitch: Double = 0.0, @JvmField var yaw: Double = 0.0) :
    Cloneable {
    val floorX: Int
        get() = kotlin.math.floor(this.pitch).toInt()

    val floorY: Int
        get() = kotlin.math.floor(this.yaw).toInt()

    fun asVector2f(): Vector2f {
        return Vector2f(this.pitch.toFloat(), this.yaw.toFloat())
    }

    fun add(x: Double): Rotator2 {
        return this.add(x, 0.0)
    }

    fun add(x: Double, y: Double): Rotator2 {
        return Rotator2(this.pitch + x, this.yaw + y)
    }

    fun add(x: Rotator2): Rotator2 {
        return this.add(x.pitch, x.yaw)
    }

    @JvmOverloads
    fun subtract(x: Double, y: Double = 0.0): Rotator2 {
        return this.add(-x, -y)
    }

    fun subtract(x: Rotator2): Rotator2 {
        return this.add(-x.pitch, -x.yaw)
    }

    fun ceil(): Rotator2 {
        return Rotator2((this.pitch + 1).toInt().toDouble(), (this.yaw + 1).toInt().toDouble())
    }

    fun floor(): Rotator2 {
        return Rotator2(
            kotlin.math.floor(this.pitch).toInt().toDouble(),
            kotlin.math.floor(this.yaw).toInt().toDouble()
        )
    }

    fun round(): Rotator2 {
        return Rotator2(Math.round(this.pitch).toDouble(), Math.round(this.yaw).toDouble())
    }

    fun abs(): Rotator2 {
        return Rotator2(kotlin.math.abs(this.pitch), kotlin.math.abs(this.yaw))
    }

    fun multiply(number: Double): Rotator2 {
        return Rotator2(this.pitch * number, this.yaw * number)
    }

    fun divide(number: Double): Rotator2 {
        return Rotator2(this.pitch / number, this.yaw / number)
    }

    @JvmOverloads
    fun distance(x: Double, y: Double = 0.0): Double {
        return sqrt(this.distanceSquared(x, y))
    }

    fun distance(vector: Rotator2): Double {
        return distance(vector.pitch, vector.yaw)
    }

    @JvmOverloads
    fun distanceSquared(x: Double, y: Double = 0.0): Double {
        val ex = this.pitch - x
        val ey = this.yaw - y
        return ey * ey + ex * ex
    }

    fun distanceSquared(vector: Rotator2): Double {
        return this.distanceSquared(vector.pitch, vector.yaw)
    }

    fun length(): Double {
        return sqrt(this.lengthSquared())
    }

    fun lengthSquared(): Double {
        return this.pitch * this.pitch + this.yaw * this.yaw
    }

    fun normalize(): Rotator2 {
        val len = this.lengthSquared()
        if (len != 0.0) {
            return this.divide(sqrt(len))
        }
        return Rotator2(0.0, 0.0)
    }

    fun dot(v: Rotator2): Double {
        return this.pitch * v.pitch + this.yaw * v.yaw
    }

    val directionVector: Vector3
        get() {
            val pitch = ((pitch + 90) * Math.PI) / 180
            val yaw = ((yaw + 90) * Math.PI) / 180
            val x = sin(pitch) * cos(yaw)
            val z = sin(pitch) * sin(yaw)
            val y = cos(pitch)
            return Vector3(x, y, z).normalize()
        }

    override fun toString(): String {
        return "Rotator2(x=" + this.pitch + ",y=" + this.yaw + ")"
    }

    public override fun clone(): Rotator2 {
        return super.clone() as Rotator2
    }

    companion object {
        val ZERO: Rotator2 = Rotator2(0.0, 0.0)
    }
}
