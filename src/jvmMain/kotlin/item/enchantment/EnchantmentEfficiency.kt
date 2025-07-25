package org.chorus_oss.chorus.item.enchantment

import org.chorus_oss.chorus.item.Item


class EnchantmentEfficiency :
    Enchantment(ID_EFFICIENCY, "digging", Rarity.COMMON, EnchantmentType.DIGGER) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 1 + (level - 1) * 10
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return super.getMinEnchantAbility(level) + 50
    }

    override val maxLevel: Int
        get() = 5

    override fun canEnchant(item: Item): Boolean {
        return item.isShears || super.canEnchant(item)
    }
}
