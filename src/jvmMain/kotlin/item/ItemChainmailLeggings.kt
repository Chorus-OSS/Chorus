package org.chorus_oss.chorus.item

class ItemChainmailLeggings : ItemArmor {
    constructor() : super(ItemID.Companion.CHAINMAIL_LEGGINGS)

    @JvmOverloads
    constructor(meta: Int, count: Int = 1) : super(
        ItemID.Companion.CHAINMAIL_LEGGINGS,
        meta,
        count,
        "Chainmail Leggings"
    )

    override val tier: Int
        get() = TIER_CHAIN

    override val isLeggings: Boolean
        get() = true

    override val armorPoints: Int
        get() = 4

    override val maxDurability: Int
        get() = 226
}