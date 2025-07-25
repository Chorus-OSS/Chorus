package org.chorus_oss.chorus.item

class ItemChainmailBoots : ItemArmor {
    constructor() : super(ItemID.Companion.CHAINMAIL_BOOTS)

    @JvmOverloads
    constructor(meta: Int, count: Int = 1) : super(ItemID.Companion.CHAINMAIL_BOOTS, meta, count, "Chainmail Boots")

    override val tier: Int
        get() = TIER_CHAIN

    override val isBoots: Boolean
        get() = true

    override val armorPoints: Int
        get() = 1

    override val maxDurability: Int
        get() = 196
}