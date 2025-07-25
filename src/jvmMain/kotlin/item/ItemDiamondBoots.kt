package org.chorus_oss.chorus.item

class ItemDiamondBoots : ItemArmor(ItemID.Companion.DIAMOND_BOOTS) {
    override val tier: Int
        get() = TIER_DIAMOND

    override val isBoots: Boolean
        get() = true

    override val armorPoints: Int
        get() = 3

    override val maxDurability: Int
        get() = 430

    override val toughness: Int
        get() = 2
}