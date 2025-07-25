package org.chorus_oss.chorus.item.enchantment.trident

import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.item.enchantment.EnchantmentType

abstract class EnchantmentTrident protected constructor(id: Int, name: String, rarity: Rarity) :
    Enchantment(id, name, rarity, EnchantmentType.TRIDENT) {
    override fun getMaxEnchantAbility(level: Int): Int {
        return 50
    }
}
