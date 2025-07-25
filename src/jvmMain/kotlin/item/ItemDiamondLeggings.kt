package org.chorus_oss.chorus.item

class ItemDiamondLeggings : ItemArmor(ItemID.Companion.DIAMOND_LEGGINGS) {
    override val isLeggings: Boolean
        get() = true

    override val tier: Int
        get() = TIER_DIAMOND

    override val armorPoints: Int
        get() = 6

    override val maxDurability: Int
        get() = 496

    override val toughness: Int
        get() = 2
}