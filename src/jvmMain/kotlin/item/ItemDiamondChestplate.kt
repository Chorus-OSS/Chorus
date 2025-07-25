package org.chorus_oss.chorus.item

class ItemDiamondChestplate : ItemArmor(ItemID.Companion.DIAMOND_CHESTPLATE) {
    override val tier: Int
        get() = TIER_DIAMOND

    override val isChestplate: Boolean
        get() = true

    override val armorPoints: Int
        get() = 8

    override val maxDurability: Int
        get() = 529

    override val toughness: Int
        get() = 2
}