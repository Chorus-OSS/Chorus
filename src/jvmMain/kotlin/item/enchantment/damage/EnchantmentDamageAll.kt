package org.chorus_oss.chorus.item.enchantment.damage

import org.chorus_oss.chorus.entity.Entity


class EnchantmentDamageAll :
    EnchantmentDamage(ID_DAMAGE_ALL, "all", Rarity.COMMON, TYPE.ALL) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 1 + (level - 1) * 11
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 20
    }

    override val maxLevel: Int
        get() = 5

    override fun getDamageBonus(target: Entity, damager: Entity): Double {
        val level = this.level
        if (level <= 0) {
            return 0.0
        }

        return level * 1.25
    }
}
