package org.chorus_oss.chorus.item.enchantment.bow


class EnchantmentBowPower :
    EnchantmentBow(ID_BOW_POWER, "arrowDamage", Rarity.COMMON) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 1 + (level - 1) * 10
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 15
    }

    override val maxLevel: Int
        get() = 5
}
