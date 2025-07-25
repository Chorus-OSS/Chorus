package org.chorus_oss.chorus.item.enchantment

class EnchantmentBindingCurse :
    Enchantment(ID_BINDING_CURSE, "curse.binding", Rarity.VERY_RARE, EnchantmentType.WEARABLE) {
    init {
        this.isObtainableFromEnchantingTable = false
    }

    override fun getMinEnchantAbility(level: Int): Int {
        return 25
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return 50
    }
}
