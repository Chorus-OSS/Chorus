package org.chorus_oss.chorus.level.util


@JvmRecord
data class BlockIndex(val x: Int, val y: Int, val z: Int, val layer: Int, val hash: Long) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as BlockIndex

        if (hash != that.hash) return false
        if (x != that.x) return false
        if (y != that.y) return false
        if (z != that.z) return false
        return layer == that.layer
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        result = 31 * result + layer
        return result
    }

    companion object {
        fun of(x: Int, y: Int, z: Int, layer: Int): BlockIndex {
            return BlockIndex(
                x,
                y,
                z,
                layer,
                (x.toLong()) shl 38 or ((y.toLong()) shl 27) or ((z.toLong()) shl 1) or layer.toLong()
            )
        }
    }
}
