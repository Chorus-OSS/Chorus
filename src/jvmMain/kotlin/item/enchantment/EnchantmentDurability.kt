package org.chorus_oss.chorus.item.enchantment

import org.chorus_oss.chorus.item.Item
import java.util.*


class EnchantmentDurability :
    Enchantment(ID_DURABILITY, "durability", Rarity.UNCOMMON, EnchantmentType.BREAKABLE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 5 + (level - 1) * 8
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return super.getMinEnchantAbility(level) + 50
    }

    override val maxLevel: Int
        get() = 3

    override fun canEnchant(item: Item): Boolean {
        return item.maxDurability >= 0 || super.canEnchant(item)
    }

    companion object {
        fun negateDamage(item: Item, level: Int, random: Random): Boolean {
            return !(item.isArmor && random.nextFloat() < 0.6f) && random.nextInt(level + 1) > 0
        }
    }
}
