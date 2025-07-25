package org.chorus_oss.chorus.item.enchantment.bow


class EnchantmentBowKnockback :
    EnchantmentBow(ID_BOW_KNOCKBACK, "arrowKnockback", Rarity.RARE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 12 + (level - 1) * 20
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 25
    }

    override val maxLevel: Int
        get() = 2
}
