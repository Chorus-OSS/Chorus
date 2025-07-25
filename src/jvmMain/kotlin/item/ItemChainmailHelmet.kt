package org.chorus_oss.chorus.item

class ItemChainmailHelmet : ItemArmor {
    constructor() : super(ItemID.Companion.CHAINMAIL_HELMET)

    @JvmOverloads
    constructor(meta: Int, count: Int = 1) : super(ItemID.Companion.CHAINMAIL_HELMET, meta, count, "Chainmail Helmet")

    override val tier: Int
        get() = TIER_CHAIN

    override val isHelmet: Boolean
        get() = true

    override val armorPoints: Int
        get() = 2

    override val maxDurability: Int
        get() = 166
}