package org.chorus_oss.chorus.item.enchantment

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID

class EnchantmentVanishingCurse : Enchantment(
    ID_VANISHING_CURSE,
    "curse.vanishing",
    Rarity.VERY_RARE,
    EnchantmentType.BREAKABLE
) {
    init {
        this.isObtainableFromEnchantingTable = false
    }

    override fun getMinEnchantAbility(level: Int): Int {
        return 25
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return 50
    }

    override fun canEnchant(item: Item): Boolean {
        return when (item.id) {
            BlockID.SKULL, ItemID.COMPASS -> true
            else -> {
                if (item.isBlock() && item.getSafeBlockState().identifier == BlockID.CARVED_PUMPKIN) {
                    return true
                }
                super.canEnchant(item)
            }
        }
    }
}
