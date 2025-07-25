package org.chorus_oss.chorus.item.enchantment.trident

class EnchantmentTridentChanneling :
    EnchantmentTrident(ID_TRIDENT_CHANNELING, "tridentChanneling", Rarity.VERY_RARE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 25
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return 50
    }
}
