package org.chorus_oss.chorus.item.enchantment.crossbow

class EnchantmentCrossbowQuickCharge :
    EnchantmentCrossbow(ID_CROSSBOW_QUICK_CHARGE, "crossbowQuickCharge", Rarity.UNCOMMON) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 12 + 20 * (level - 1)
    }

    override val maxLevel: Int
        get() = 3
}
