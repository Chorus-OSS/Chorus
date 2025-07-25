package org.chorus_oss.chorus.item.enchantment

import org.chorus_oss.chorus.entity.Entity


class EnchantmentDensity : Enchantment(ID_DENSITY, "density", Rarity.RARE, EnchantmentType.MACE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 10 + (level - 1) * 20
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return super.getMinEnchantAbility(level) + 50
    }

    override val maxLevel: Int
        get() = 5

    override fun checkCompatibility(enchantment: Enchantment): Boolean {
        val id = enchantment.id
        return id != ID_DAMAGE_ALL && id != ID_BREACH && id != ID_DAMAGE_SMITE && id != ID_DAMAGE_ARTHROPODS
    }

    override fun getDamageBonus(target: Entity, damager: Entity): Double {
        val height = damager.highestPosition - damager.position.y
        if (height >= 1.5f) {
            return height * 0.5f * this.level
        }
        return 0.0
    }
}
