package org.chorus_oss.chorus.math

import kotlin.math.sqrt


class Vector2 @JvmOverloads constructor(@JvmField var x: Double = 0.0, @JvmField var y: Double = 0.0) {
    val floorX: Int
        get() = kotlin.math.floor(this.x).toInt()

    val floorY: Int
        get() = kotlin.math.floor(this.y).toInt()

    fun add(x: Double): Vector2 {
        return this.add(x, 0.0)
    }

    fun add(x: Double, y: Double): Vector2 {
        return Vector2(this.x + x, this.y + y)
    }

    fun add(x: Vector2): Vector2 {
        return this.add(x.x, x.y)
    }

    @JvmOverloads
    fun subtract(x: Double, y: Double = 0.0): Vector2 {
        return this.add(-x, -y)
    }

    fun subtract(x: Vector2): Vector2 {
        return this.add(-x.x, -x.y)
    }

    fun ceil(): Vector2 {
        return Vector2((this.x + 1).toInt().toDouble(), (this.y + 1).toInt().toDouble())
    }

    fun floor(): Vector2 {
        return Vector2(kotlin.math.floor(this.x).toInt().toDouble(), kotlin.math.floor(this.y).toInt().toDouble())
    }

    fun round(): Vector2 {
        return Vector2(Math.round(this.x).toDouble(), Math.round(this.y).toDouble())
    }

    fun abs(): Vector2 {
        return Vector2(kotlin.math.abs(this.x), kotlin.math.abs(this.y))
    }

    fun multiply(number: Double): Vector2 {
        return Vector2(this.x * number, this.y * number)
    }

    fun divide(number: Double): Vector2 {
        return Vector2(this.x / number, this.y / number)
    }

    @JvmOverloads
    fun distance(x: Double, y: Double = 0.0): Double {
        return sqrt(this.distanceSquared(x, y))
    }

    fun distance(vector: Vector2): Double {
        return distance(vector.x, vector.y)
    }

    @JvmOverloads
    fun distanceSquared(x: Double, y: Double = 0.0): Double {
        val ex = this.x - x
        val ey = this.y - y
        return ey * ey + ex * ex
    }

    fun distanceSquared(vector: Vector2): Double {
        return this.distanceSquared(vector.x, vector.y)
    }

    fun length(): Double {
        return sqrt(this.lengthSquared())
    }

    fun lengthSquared(): Double {
        return this.x * this.x + this.y * this.y
    }

    fun normalize(): Vector2 {
        val len = this.lengthSquared()
        if (len != 0.0) {
            return this.divide(sqrt(len))
        }
        return Vector2(0.0, 0.0)
    }

    fun dot(v: Vector2): Double {
        return this.x * v.x + this.y * v.y
    }

    override fun toString(): String {
        return "Vector2(x=" + this.x + ",y=" + this.y + ")"
    }

    companion object {
        val ZERO: Vector2 = Vector2(0.0, 0.0)
    }
}
