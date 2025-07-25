package org.chorus_oss.chorus.item.enchantment.loot

import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.item.enchantment.EnchantmentType


abstract class EnchantmentLoot protected constructor(id: Int, name: String, rarity: Rarity, type: EnchantmentType) :
    Enchantment(id, name, rarity, type) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 15 + (level - 1) * 9
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return getMinEnchantAbility(level) + 45 + level
    }

    override val maxLevel: Int
        get() = 3

    public override fun checkCompatibility(enchantment: Enchantment): Boolean {
        return super.checkCompatibility(enchantment) && enchantment.id != ID_SILK_TOUCH
    }
}
