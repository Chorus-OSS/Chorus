package org.chorus_oss.chorus.item.enchantment

class EnchantmentFrostWalker :
    Enchantment(ID_FROST_WALKER, "frostwalker", Rarity.VERY_RARE, EnchantmentType.ARMOR_FEET) {
    init {
        this.isObtainableFromEnchantingTable = false
    }

    override fun getMinEnchantAbility(level: Int): Int {
        return level * 10
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 15
    }

    override val maxLevel: Int
        get() = 2

    override fun checkCompatibility(enchantment: Enchantment): Boolean {
        return super.checkCompatibility(enchantment) && enchantment.id != ID_WATER_WALKER
    }
}
