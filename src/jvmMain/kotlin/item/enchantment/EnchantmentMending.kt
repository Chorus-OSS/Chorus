package org.chorus_oss.chorus.item.enchantment

class EnchantmentMending :
    Enchantment(ID_MENDING, "mending", Rarity.RARE, EnchantmentType.BREAKABLE) {
    init {
        this.isObtainableFromEnchantingTable = false
    }

    override fun getMinEnchantAbility(level: Int): Int {
        return 25 * level
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 50
    }

    public override fun checkCompatibility(enchantment: Enchantment): Boolean {
        return super.checkCompatibility(enchantment) && enchantment.id != ID_BOW_INFINITY
    }
}