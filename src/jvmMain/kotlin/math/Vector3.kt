package org.chorus_oss.chorus.math


import kotlin.math.max
import kotlin.math.sqrt


open class Vector3 @JvmOverloads constructor(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) :
    Cloneable, IVector3 {
    override val vector3: Vector3
        get() = Vector3(x, y, z)

    fun setX(x: Double): Vector3 {
        this.x = x
        return this
    }

    fun setY(y: Double): Vector3 {
        this.y = y
        return this
    }

    fun setZ(z: Double): Vector3 {
        this.z = z
        return this
    }

    val floorX: Int
        get() = kotlin.math.floor(this.x).toInt()

    val floorY: Int
        get() = kotlin.math.floor(this.y).toInt()

    val floorZ: Int
        get() = kotlin.math.floor(this.z).toInt()

    val chunkX: Int
        get() = floorX shr 4

    val chunkZ: Int
        get() = floorZ shr 4

    val chunkSectionY: Int
        get() = floorY shr 4

    val chunkVector: ChunkVector2
        get() = ChunkVector2(chunkX, chunkZ)

    open fun add(x: Double): Vector3 {
        return this.add(x, 0.0, 0.0)
    }

    open fun add(x: Double, y: Double): Vector3 {
        return this.add(x, y, 0.0)
    }

    open fun add(x: Double, y: Double, z: Double): Vector3 {
        return Vector3(this.x + x, this.y + y, this.z + z)
    }

    open fun add(x: Vector3): Vector3 {
        return Vector3(this.x + x.x, this.y + x.y, this.z + x.z)
    }

    open fun subtract(x: Double): Vector3 {
        return this.subtract(x, 0.0, 0.0)
    }

    open fun subtract(x: Double, y: Double): Vector3 {
        return this.subtract(x, y, 0.0)
    }

    open fun subtract(x: Double, y: Double, z: Double): Vector3 {
        return this.add(-x, -y, -z)
    }

    open fun subtract(x: Vector3): Vector3 {
        return this.add(-x.x, -x.y, -x.z)
    }

    open fun multiply(number: Double): Vector3 {
        return Vector3(this.x * number, this.y * number, this.z * number)
    }

    open fun divide(number: Double): Vector3 {
        return Vector3(this.x / number, this.y / number, this.z / number)
    }

    open fun ceil(): Vector3 {
        return Vector3(
            kotlin.math.ceil(this.x).toInt().toDouble(),
            kotlin.math.ceil(this.y).toInt().toDouble(),
            kotlin.math.ceil(
                this.z
            ).toInt().toDouble()
        )
    }

    open fun floor(): Vector3 {
        return Vector3(
            floorX.toDouble(),
            floorY.toDouble(),
            floorZ.toDouble()
        )
    }

    open fun round(): Vector3 {
        return Vector3(
            Math.round(this.x).toDouble(), Math.round(this.y).toDouble(), Math.round(
                this.z
            ).toDouble()
        )
    }

    open fun abs(): Vector3 {
        return Vector3(
            kotlin.math.abs(this.x).toInt().toDouble(),
            kotlin.math.abs(this.y).toInt().toDouble(),
            kotlin.math.abs(
                this.z
            ).toInt().toDouble()
        )
    }

    open fun getSide(face: BlockFace): Vector3 {
        return this.getSide(face, 1)
    }

    open fun getSide(face: BlockFace, step: Int): Vector3 {
        return Vector3(
            this.x + face.xOffset * step,
            y + face.yOffset * step,
            z + face.zOffset * step
        )
    }

    // Get as a Vector3 for better performance. Do not override in Block!
    fun getSideVec(face: BlockFace): Vector3 {
        return Vector3(
            this.x + face.xOffset,
            y + face.yOffset,
            z + face.zOffset
        )
    }

    open fun up(): Vector3 {
        return up(1)
    }

    open fun up(step: Int): Vector3 {
        return getSide(BlockFace.UP, step)
    }

    open fun down(): Vector3 {
        return down(1)
    }

    open fun down(step: Int): Vector3 {
        return getSide(BlockFace.DOWN, step)
    }

    open fun north(): Vector3 {
        return north(1)
    }

    open fun north(step: Int): Vector3 {
        return getSide(BlockFace.NORTH, step)
    }

    open fun south(): Vector3 {
        return south(1)
    }

    open fun south(step: Int): Vector3 {
        return getSide(BlockFace.SOUTH, step)
    }

    open fun east(): Vector3 {
        return east(1)
    }

    open fun east(step: Int): Vector3 {
        return getSide(BlockFace.EAST, step)
    }

    open fun west(): Vector3 {
        return west(1)
    }

    open fun west(step: Int): Vector3 {
        return getSide(BlockFace.WEST, step)
    }

    fun distanceManhattan(pos: Vector3): Int {
        val x = kotlin.math.abs(pos.x - this.x)
        val y = kotlin.math.abs(pos.y - this.y)
        val z = kotlin.math.abs(pos.z - this.z)
        return (x + y + z).toInt()
    }

    fun distance(pos: Vector3): Double {
        return distance(pos.x, pos.y, pos.z)
    }

    fun distanceSquared(pos: Vector3): Double {
        return distanceSquared(pos.x, pos.y, pos.z)
    }

    fun distance(x: Double, y: Double, z: Double): Double {
        return sqrt(distanceSquared(x, y, z))
    }

    fun distanceSquared(x: Double, y: Double, z: Double): Double {
        val ex = this.x - x
        val ey = this.y - y
        val ez = this.z - z
        return ex * ex + ey * ey + ez * ez
    }

    @JvmOverloads
    fun maxPlainDistance(x: Double = 0.0, z: Double = 0.0): Double {
        return max(kotlin.math.abs(this.x - x), kotlin.math.abs(this.z - z))
    }

    fun maxPlainDistance(vector: Vector2): Double {
        return this.maxPlainDistance(vector.x, vector.y)
    }

    fun maxPlainDistance(x: Vector3): Double {
        return this.maxPlainDistance(x.x, x.z)
    }

    fun toHorizontal(): Vector2 {
        return Vector2(this.x, this.z)
    }

    /**
     * Calculates the Length of this Vector
     *
     * @return The Length of this Vector.
     */
    fun length(): Double {
        return sqrt(this.lengthSquared())
    }

    fun lengthSquared(): Double {
        return this.x * this.x + this.y * this.y + this.z * this.z
    }

    fun normalize(): Vector3 {
        val len = this.lengthSquared()
        if (len > 0) {
            return this.divide(sqrt(len))
        }
        return Vector3(0.0, 0.0, 0.0)
    }

    /**
     * Scalar Product of this Vector and the Vector supplied.
     *
     * @param v Vector to calculate the scalar product to.
     * @return Scalar Product
     */
    fun dot(v: Vector3): Double {
        return this.x * v.x + this.y * v.y + this.z * v.z
    }

    /**
     * Calculates the cross product of this Vector and the given Vector
     *
     * @param v the vector to calculate the cross product with.
     * @return a Vector at right angle to this and other
     */
    fun cross(v: Vector3): Vector3 {
        return Vector3(
            this.y * v.z - this.z * v.y,
            this.z * v.x - this.x * v.z,
            this.x * v.y - this.y * v.x
        )
    }

    /* PowerNukkit: The Angle class was removed because it had all rights reserved copyright on it.
     * Calculates the angle between this and the supplied Vector.
     *
     * @param v the Vector to calculate the angle to.
     * @return the Angle between the two Vectors.
     */
    /*public Angle angleBetween(Vector3 v) {
        return Angle.fromRadian(Math.acos(Math.min(Math.max(this.normalize().dot(v.normalize()), -1.0d), 1.0d)));
    }*/
    /**
     * Returns a new vector with x value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     *
     * @param v vector
     * @param x x value
     * @return intermediate vector
     */
    open fun getIntermediateWithXValue(v: Vector3, x: Double): Vector3? {
        val xDiff = v.x - this.x
        val yDiff = v.y - this.y
        val zDiff = v.z - this.z
        if (xDiff * xDiff < 0.0000001) {
            return null
        }
        val f = (x - this.x) / xDiff
        return if (f < 0 || f > 1) {
            null
        } else {
            Vector3(
                this.x + xDiff * f,
                y + yDiff * f,
                z + zDiff * f
            )
        }
    }

    /**
     * Returns a new vector with y value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     *
     * @param v vector
     * @param y y value
     * @return intermediate vector
     */
    open fun getIntermediateWithYValue(v: Vector3, y: Double): Vector3? {
        val xDiff = v.x - this.x
        val yDiff = v.y - this.y
        val zDiff = v.z - this.z
        if (yDiff * yDiff < 0.0000001) {
            return null
        }
        val f = (y - this.y) / yDiff
        return if (f < 0 || f > 1) {
            null
        } else {
            Vector3(
                this.x + xDiff * f,
                this.y + yDiff * f,
                z + zDiff * f
            )
        }
    }

    /**
     * Returns a new vector with z value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     *
     * @param v vector
     * @param z z value
     * @return intermediate vector
     */
    open fun getIntermediateWithZValue(v: Vector3, z: Double): Vector3? {
        val xDiff = v.x - this.x
        val yDiff = v.y - this.y
        val zDiff = v.z - this.z
        if (zDiff * zDiff < 0.0000001) {
            return null
        }
        val f = (z - this.z) / zDiff
        return if (f < 0 || f > 1) {
            null
        } else {
            Vector3(
                this.x + xDiff * f,
                y + yDiff * f,
                this.z + zDiff * f
            )
        }
    }

    open fun setComponents(x: Double, y: Double, z: Double): Vector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun setComponentsAdding(x: Double, y: Double, z: Double, ax: Double, ay: Double, az: Double): Vector3 {
        this.x = x + ax
        this.y = y + ay
        this.z = z + az
        return this
    }

    fun setComponentsAdding(pos: Vector3, face: BlockFace): Vector3 {
        return setComponentsAdding(
            pos.x,
            pos.y,
            pos.z,
            face.xOffset.toDouble(),
            face.yOffset.toDouble(),
            face.zOffset.toDouble()
        )
    }

    open fun setComponents(pos: Vector3): Vector3 {
        this.x = pos.x
        this.y = pos.y
        this.z = pos.z
        return this
    }

    fun getAxis(axis: BlockFace.Axis): Double {
        return when (axis) {
            BlockFace.Axis.X -> x
            BlockFace.Axis.Y -> y
            else -> z
        }
    }

    override fun toString(): String {
        return "Vector3(x=" + this.x + ",y=" + this.y + ",z=" + this.z + ")"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Vector3) {
            return false
        }

        return this.x == other.x && this.y == other.y && this.z == other.z
    }

    override fun hashCode(): Int {
        return (x.toInt() xor (z.toInt() shl 12)) xor (y.toInt() shl 24)
    }

    fun rawHashCode(): Int {
        return super.hashCode()
    }

    public override fun clone(): Vector3 {
        return super.clone() as Vector3
    }

    fun asVector3f(): Vector3f {
        return Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun asBlockVector3(): BlockVector3 {
        return BlockVector3(this.floorX, this.floorY, this.floorZ)
    }

    companion object {
        val ZERO: Vector3 = Vector3(0.0, 0.0, 0.0)
    }
}
