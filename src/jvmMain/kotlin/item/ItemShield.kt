package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.DyeColor

class ItemShield : ItemTool {
    @JvmOverloads
    constructor(meta: Int = 0, count: Int = 1) : super(ItemID.Companion.SHIELD, meta, count, "Shield")

    /**
     * 为自定义盾牌提供的构造函数
     *
     *
     * Constructor for custom shield
     *
     * @param id    the id
     * @param meta  the meta
     * @param count the count
     * @param name  the name
     */
    protected constructor(id: String, meta: Int, count: Int, name: String?) : super(id, meta, count, name)

    fun hasBannerPattern(): Boolean {
        return this.hasCompoundTag() && (this.namedTag!!.containsInt("Base") ||
                this.namedTag!!.containsInt("Type") || this.namedTag!!.containsList("Patterns"))
    }

    var bannerPattern: ItemBanner?
        get() {
            if (!this.hasBannerPattern()) {
                return null
            }
            val tag = this.namedTag
            val item = ItemBanner()
            for ((key, value) in item.namedTag!!.entrySet) {
                tag!!.put(key, value)
            }
            if (this.namedTag!!.containsInt("Base")) {
                item.setBaseColor(DyeColor.getByDyeData(this.namedTag!!.getInt("Base")))
            }
            return item
        }
        set(banner) {
            if (banner == null) {
                this.clearNamedTag()
                return
            }
            val tag = if (!hasBannerPattern()) {
                CompoundTag()
            } else {
                this.namedTag
            }
            for ((key, value) in banner.namedTag!!.entrySet) {
                tag!!.put(key, value)
            }
            tag!!.putInt("Base", banner.baseColor)
            this.setNamedTag(tag)
        }

    override val maxStackSize: Int
        get() = 1

    override val maxDurability: Int
        get() = DURABILITY_SHIELD
}
