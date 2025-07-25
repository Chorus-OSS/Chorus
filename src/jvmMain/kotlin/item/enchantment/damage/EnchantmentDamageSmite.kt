package org.chorus_oss.chorus.item.enchantment.damage

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntitySmite


class EnchantmentDamageSmite :
    EnchantmentDamage(ID_DAMAGE_SMITE, "undead", Rarity.UNCOMMON, TYPE.SMITE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 5 + (level - 1) * 8
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 20
    }

    override fun getDamageBonus(target: Entity, damager: Entity): Double {
        if (target is EntitySmite) {
            return this.level * 2.5
        }

        return 0.0
    }
}
