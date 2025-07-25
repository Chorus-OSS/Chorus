package org.chorus_oss.chorus.item

class ItemChainmailChestplate : ItemArmor {
    constructor() : super(ItemID.Companion.CHAINMAIL_CHESTPLATE)

    @JvmOverloads
    constructor(meta: Int, count: Int = 1) : super(
        ItemID.Companion.CHAINMAIL_CHESTPLATE,
        meta,
        count,
        "Chainmail Chestplate"
    )

    override val tier: Int
        get() = TIER_CHAIN

    override val isChestplate: Boolean
        get() = true

    override val armorPoints: Int
        get() = 5

    override val maxDurability: Int
        get() = 241
}