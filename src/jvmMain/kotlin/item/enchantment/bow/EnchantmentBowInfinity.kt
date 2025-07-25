package org.chorus_oss.chorus.item.enchantment.bow

import org.chorus_oss.chorus.item.enchantment.Enchantment


class EnchantmentBowInfinity :
    EnchantmentBow(ID_BOW_INFINITY, "arrowInfinite", Rarity.VERY_RARE) {
    override fun checkCompatibility(enchantment: Enchantment): Boolean {
        return super.checkCompatibility(enchantment) && enchantment.id != ID_MENDING
    }

    override fun getMinEnchantAbility(level: Int): Int {
        return 20
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return 50
    }
}
