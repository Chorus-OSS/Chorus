package org.chorus_oss.chorus.item.enchantment


class EnchantmentKnockback :
    Enchantment(ID_KNOCKBACK, "knockback", Rarity.UNCOMMON, EnchantmentType.SWORD) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 5 + (level - 1) * 20
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return super.getMinEnchantAbility(level) + 50
    }

    override val maxLevel: Int
        get() = 2
}
