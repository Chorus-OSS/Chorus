package org.chorus_oss.chorus.math


import kotlin.math.pow
import kotlin.math.sqrt

class BlockVector3 : Cloneable {
    @JvmField
    var x: Int = 0

    @JvmField
    var y: Int = 0

    @JvmField
    var z: Int = 0

    constructor(x: Int, y: Int, z: Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor()

    fun setComponents(x: Int, y: Int, z: Int): BlockVector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun setComponentsAdding(pos: Vector3, face: BlockFace): BlockVector3 {
        this.x = pos.floorX + face.xOffset
        this.y = pos.floorY + face.yOffset
        this.z = pos.floorZ + face.zOffset
        return this
    }

    fun setX(x: Int): BlockVector3 {
        this.x = x
        return this
    }

    fun setY(y: Int): BlockVector3 {
        this.y = y
        return this
    }

    fun setZ(z: Int): BlockVector3 {
        this.z = z
        return this
    }

    fun add(x: Double): Vector3 {
        return this.add(x, 0.0, 0.0)
    }

    fun add(x: Double, y: Double): Vector3 {
        return this.add(x, y, 0.0)
    }

    fun add(x: Double, y: Double, z: Double): Vector3 {
        return Vector3(this.x + x, this.y + y, this.z + z)
    }

    fun add(x: Vector3): Vector3 {
        return Vector3(this.x + x.x, this.y + x.y, this.z + x.z)
    }

    @JvmOverloads
    fun subtract(x: Double, y: Double = 0.0, z: Double = 0.0): Vector3 {
        return this.add(-x, -y, -z)
    }

    fun subtract(x: Vector3): Vector3 {
        return this.add(-x.x, -x.y, -x.z)
    }

    fun add(x: Int): BlockVector3 {
        return this.add(x, 0, 0)
    }

    fun add(x: Int, y: Int): BlockVector3 {
        return this.add(x, y, 0)
    }

    fun add(x: Int, y: Int, z: Int): BlockVector3 {
        return BlockVector3(this.x + x, this.y + y, this.z + z)
    }

    fun add(x: BlockVector3): BlockVector3 {
        return BlockVector3(this.x + x.x, this.y + x.y, this.z + x.z)
    }

    @JvmOverloads
    fun subtract(x: Int = 0, y: Int = 0, z: Int = 0): BlockVector3 {
        return this.add(-x, -y, -z)
    }

    fun subtract(x: BlockVector3): BlockVector3 {
        return this.add(-x.x, -x.y, -x.z)
    }

    fun multiply(number: Int): BlockVector3 {
        return BlockVector3(this.x * number, this.y * number, this.z * number)
    }

    fun divide(number: Int): BlockVector3 {
        return BlockVector3(this.x / number, this.y / number, this.z / number)
    }

    fun getSide(face: BlockFace): BlockVector3 {
        return this.getSide(face, 1)
    }

    fun getSide(face: BlockFace, step: Int): BlockVector3 {
        return BlockVector3(
            this.x + face.xOffset * step,
            y + face.yOffset * step,
            z + face.zOffset * step
        )
    }

    @JvmOverloads
    fun up(step: Int = 1): BlockVector3 {
        return getSide(BlockFace.UP, step)
    }

    @JvmOverloads
    fun down(step: Int = 1): BlockVector3 {
        return getSide(BlockFace.DOWN, step)
    }

    @JvmOverloads
    fun north(step: Int = 1): BlockVector3 {
        return getSide(BlockFace.NORTH, step)
    }

    @JvmOverloads
    fun south(step: Int = 1): BlockVector3 {
        return getSide(BlockFace.SOUTH, step)
    }

    @JvmOverloads
    fun east(step: Int = 1): BlockVector3 {
        return getSide(BlockFace.EAST, step)
    }

    @JvmOverloads
    fun west(step: Int = 1): BlockVector3 {
        return getSide(BlockFace.WEST, step)
    }

    fun distance(pos: Vector3): Double {
        return sqrt(this.distanceSquared(pos))
    }

    fun distance(pos: BlockVector3): Double {
        return sqrt(this.distanceSquared(pos))
    }

    fun distanceSquared(pos: Vector3): Double {
        return distanceSquared(pos.x, pos.y, pos.z)
    }

    fun distanceSquared(pos: BlockVector3): Double {
        return distanceSquared(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
    }

    fun distanceSquared(x: Double, y: Double, z: Double): Double {
        return (this.x - x).pow(2.0) + (this.y - y).pow(2.0) + (this.z - z).pow(2.0)
    }

    val chunkX: Int
        get() = x shr 4

    val chunkZ: Int
        get() = z shr 4

    val chunkSectionY: Int
        get() = y shr 4

    fun getChunkSectionY(is384World: Boolean): Int {
        return (y shr 4) + (if (is384World) 4 else 0)
    }

    val chunkVector: ChunkVector2
        get() = ChunkVector2(chunkX, chunkZ)

    fun getAxis(axis: BlockFace.Axis): Int {
        return when (axis) {
            BlockFace.Axis.X -> x
            BlockFace.Axis.Y -> y
            else -> z
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true

        if (other !is BlockVector3) return false

        return this.x == other.x && this.y == other.y && this.z == other.z
    }

    override fun hashCode(): Int {
        return (x xor (z shl 12)) xor (y shl 24)
    }

    override fun toString(): String {
        return "BlockPosition(level=" + ",x=" + this.x + ",y=" + this.y + ",z=" + this.z + ")"
    }

    public override fun clone(): BlockVector3 {
        return super.clone() as BlockVector3
    }

    fun asVector3(): Vector3 {
        return Vector3(x.toDouble(), y.toDouble(), z.toDouble())
    }

    fun asVector3f(): Vector3f {
        return Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
    }
}
