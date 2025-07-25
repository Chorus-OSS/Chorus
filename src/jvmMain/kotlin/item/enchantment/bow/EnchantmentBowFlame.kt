package org.chorus_oss.chorus.item.enchantment.bow


class EnchantmentBowFlame :
    EnchantmentBow(ID_BOW_FLAME, "arrowFire", Rarity.RARE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 20
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return 50
    }
}
