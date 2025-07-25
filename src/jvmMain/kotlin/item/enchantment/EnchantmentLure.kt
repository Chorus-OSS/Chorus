package org.chorus_oss.chorus.item.enchantment


class EnchantmentLure :
    Enchantment(ID_LURE, "fishingSpeed", Rarity.RARE, EnchantmentType.FISHING_ROD) {
    override fun getMinEnchantAbility(level: Int): Int {
        return level + 8 * level + 6
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return getMinEnchantAbility(level) + 45 + level
    }

    override val maxLevel: Int
        get() = 3
}
