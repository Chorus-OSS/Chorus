package org.chorus_oss.chorus.item

class ItemDiamondHelmet : ItemArmor(ItemID.Companion.DIAMOND_HELMET) {
    override val tier: Int
        get() = TIER_DIAMOND

    override val isHelmet: Boolean
        get() = true

    override val armorPoints: Int
        get() = 3

    override val maxDurability: Int
        get() = 364

    override val toughness: Int
        get() = 2
}