package org.chorus_oss.chorus.item.enchantment.crossbow

import org.chorus_oss.chorus.item.enchantment.Enchantment

class EnchantmentCrossbowMultishot :
    EnchantmentCrossbow(ID_CROSSBOW_MULTISHOT, "crossbowMultishot", Rarity.RARE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 20
    }

    override val maxLevel: Int
        get() = 1

    public override fun checkCompatibility(enchantment: Enchantment): Boolean {
        return super.checkCompatibility(enchantment) && enchantment.id != ID_CROSSBOW_PIERCING
    }
}
