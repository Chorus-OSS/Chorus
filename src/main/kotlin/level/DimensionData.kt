package org.chorus_oss.chorus.level

import java.util.*

class DimensionData @JvmOverloads constructor(
    val dimensionName: String,
    val dimensionId: Int,
    @JvmField val minHeight: Int,
    @JvmField val maxHeight: Int,
    chunkSectionCount: Int? = null
) {
    val height: Int
    val chunkSectionCount: Int

    @JvmOverloads
    constructor(dimensionId: Int, minHeight: Int, maxHeight: Int, chunkSectionCount: Int? = null) : this(
        when (dimensionId) {
            1 -> "minecraft:nether"
            2 -> "minecraft:the_end"
            else -> "minecraft:overworld"
        }, dimensionId, minHeight, maxHeight, chunkSectionCount
    )

    init {
        var height = maxHeight - minHeight
        if (minHeight <= 0 && maxHeight > 0) {
            height += 1 // 0 y coordinate counts too
        }
        this.height = height

        this.chunkSectionCount = Objects.requireNonNullElseGet(
            chunkSectionCount
        ) { this.height shr 4 + (if ((this.height and 15) == 0) 0 else 1) }
    }

    val minSectionY: Int
        get() = this.minHeight shr 4

    val maxSectionY: Int
        get() = this.maxHeight shr 4

    val suffix: String
        get() = when (dimensionId) {
            1 -> "_the_nether"
            2 -> "_the_end"
            else -> ""
        }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is DimensionData) return false
        return dimensionId == o.dimensionId && minHeight == o.minHeight && maxHeight == o.maxHeight && height == o.height && chunkSectionCount == o.chunkSectionCount && dimensionName == o.dimensionName
    }

    override fun toString(): String {
        return "DimensionData(dimensionName=" + this.dimensionName + ", dimensionId=" + this.dimensionId + ", minHeight=" + this.minHeight + ", maxHeight=" + this.maxHeight + ", height=" + this.height + ", chunkSectionCount=" + this.chunkSectionCount + ")"
    }
}