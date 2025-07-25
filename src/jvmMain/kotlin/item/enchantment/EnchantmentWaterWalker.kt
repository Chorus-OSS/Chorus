package org.chorus_oss.chorus.item.enchantment


class EnchantmentWaterWalker :
    Enchantment(ID_WATER_WALKER, "waterWalker", Rarity.RARE, EnchantmentType.ARMOR_FEET) {
    override fun getMinEnchantAbility(level: Int): Int {
        return level * 10
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 15
    }

    override val maxLevel: Int
        get() = 3
}
