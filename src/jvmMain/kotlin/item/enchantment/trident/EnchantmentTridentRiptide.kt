package org.chorus_oss.chorus.item.enchantment.trident

import org.chorus_oss.chorus.item.enchantment.Enchantment

class EnchantmentTridentRiptide :
    EnchantmentTrident(ID_TRIDENT_RIPTIDE, "tridentRiptide", Rarity.RARE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 7 * level + 10
    }

    override val maxLevel: Int
        get() = 3

    public override fun checkCompatibility(enchantment: Enchantment): Boolean {
        return super.checkCompatibility(enchantment)
                && enchantment.id != ID_TRIDENT_LOYALTY && enchantment.id != ID_TRIDENT_CHANNELING
    }
}
