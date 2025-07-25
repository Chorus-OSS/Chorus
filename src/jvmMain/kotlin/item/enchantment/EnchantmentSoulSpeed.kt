package org.chorus_oss.chorus.item.enchantment


class EnchantmentSoulSpeed :
    Enchantment(ID_SOUL_SPEED, "soul_speed", Rarity.VERY_RARE, EnchantmentType.ARMOR_FEET) {
    init {
        this.isObtainableFromEnchantingTable = false
    }

    override fun getMinEnchantAbility(level: Int): Int {
        return 10 * level
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return getMinEnchantAbility(level) + 15
    }

    override val maxLevel: Int
        get() = 3
}
