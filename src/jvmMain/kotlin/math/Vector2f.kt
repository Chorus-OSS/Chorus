package org.chorus_oss.chorus.math

import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt


class Vector2f @JvmOverloads constructor(@JvmField var x: Float = 0f, @JvmField var y: Float = 0f) {
    val floorX: Int
        get() = floor(this.x).toInt()

    val floorY: Int
        get() = floor(this.y).toInt()

    fun add(x: Float): Vector2f {
        return this.add(x, 0f)
    }

    fun add(x: Float, y: Float): Vector2f {
        return Vector2f(this.x + x, this.y + y)
    }

    fun add(x: Vector2f): Vector2f {
        return this.add(x.x, x.y)
    }

    @JvmOverloads
    fun subtract(x: Float, y: Float = 0f): Vector2f {
        return this.add(-x, -y)
    }

    fun subtract(x: Vector2f): Vector2f {
        return this.add(-x.x, -x.y)
    }

    fun ceil(): Vector2f {
        return Vector2f((this.x + 1).toInt().toFloat(), (this.y + 1).toInt().toFloat())
    }

    fun floor(): Vector2f {
        return Vector2f(floorX.toFloat(), floorY.toFloat())
    }

    fun round(): Vector2f {
        return Vector2f(Math.round(this.x).toFloat(), Math.round(this.y).toFloat())
    }

    fun abs(): Vector2f {
        return Vector2f(kotlin.math.abs(x.toDouble()).toFloat(), kotlin.math.abs(y.toDouble()).toFloat())
    }

    fun multiply(number: Float): Vector2f {
        return Vector2f(this.x * number, this.y * number)
    }

    fun divide(number: Float): Vector2f {
        return Vector2f(this.x / number, this.y / number)
    }

    @JvmOverloads
    fun distance(x: Float, y: Float = 0f): Double {
        return sqrt(this.distanceSquared(x, y))
    }

    fun distance(vector: Vector2f): Double {
        return sqrt(this.distanceSquared(vector.x, vector.y))
    }

    @JvmOverloads
    fun distanceSquared(x: Float, y: Float = 0f): Double {
        return (this.x - x).toDouble().pow(2.0) + (this.y - y).toDouble().pow(2.0)
    }

    fun distanceSquared(vector: Vector2f): Double {
        return this.distanceSquared(vector.x, vector.y)
    }

    fun length(): Double {
        return sqrt(lengthSquared().toDouble())
    }

    fun lengthSquared(): Float {
        return this.x * this.x + this.y * this.y
    }

    fun normalize(): Vector2f {
        val len = this.lengthSquared()
        if (len != 0f) {
            return this.divide(sqrt(len.toDouble()).toFloat())
        }
        return Vector2f(0f, 0f)
    }

    fun dot(v: Vector2f): Float {
        return this.x * v.x + this.y * v.y
    }

    override fun toString(): String {
        return "Vector2(x=" + this.x + ",y=" + this.y + ")"
    }
}
