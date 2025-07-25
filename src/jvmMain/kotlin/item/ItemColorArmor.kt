package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.BlockColor
import org.chorus_oss.chorus.utils.DyeColor

abstract class ItemColorArmor : ItemArmor {
    constructor(id: String) : super(id)

    constructor(id: String, meta: Int) : super(id, meta)

    constructor(id: String, meta: Int, count: Int) : super(id, meta, count)

    constructor(id: String, meta: Int, count: Int, name: String?) : super(id, meta, count, name)

    /**
     * Set leather armor color
     *
     * @param dyeColor - DyeColor object
     * @return - Return colored item
     */
    fun setColor(dyeColor: DyeColor): ItemColorArmor {
        val blockColor = dyeColor.color
        return setColor(blockColor.red, blockColor.green, blockColor.blue)
    }

    /**
     * Set leather armor color
     *
     * @param color - BlockColor object
     * @return - Return colored item
     */
    fun setColor(color: BlockColor): ItemColorArmor {
        return setColor(color.red, color.green, color.blue)
    }

    /**
     * Set leather armor color
     *
     * @param r - red
     * @param g - green
     * @param b - blue
     * @return - Return colored item
     */
    fun setColor(r: Int, g: Int, b: Int): ItemColorArmor {
        val rgb = r shl 16 or (g shl 8) or b
        val tag = if (this.hasCompoundTag()) this.namedTag else CompoundTag()
        tag!!.putInt("customColor", rgb)
        this.setNamedTag(tag)
        return this
    }

    val color: BlockColor?
        /**
         * Get color of Leather Item
         *
         * @return - BlockColor, or null if item has no color
         */
        get() {
            if (!this.hasCompoundTag()) return null
            val tag = this.namedTag
            if (!tag!!.exist("customColor")) return null
            val rgb = tag.getInt("customColor")
            return BlockColor(rgb)
        }
}
