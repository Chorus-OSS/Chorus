package org.chorus_oss.chorus.item.enchantment.crossbow

import org.chorus_oss.chorus.item.enchantment.Enchantment

class EnchantmentCrossbowPiercing :
    EnchantmentCrossbow(ID_CROSSBOW_PIERCING, "crossbowPiercing", Rarity.COMMON) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 1 + 10 * (level - 1)
    }

    override val maxLevel: Int
        get() = 4

    public override fun checkCompatibility(enchantment: Enchantment): Boolean {
        return super.checkCompatibility(enchantment) && enchantment.id != ID_CROSSBOW_MULTISHOT
    }
}
