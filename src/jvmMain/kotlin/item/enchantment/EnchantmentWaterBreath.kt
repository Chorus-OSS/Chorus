package org.chorus_oss.chorus.item.enchantment


class EnchantmentWaterBreath :
    Enchantment(ID_WATER_BREATHING, "oxygen", Rarity.RARE, EnchantmentType.ARMOR_HEAD) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 10 * level
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 30
    }

    override val maxLevel: Int
        get() = 3
}
