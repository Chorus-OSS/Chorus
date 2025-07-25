package org.chorus_oss.chorus.item.enchantment.trident

import org.chorus_oss.chorus.entity.Entity

class EnchantmentTridentImpaling :
    EnchantmentTrident(ID_TRIDENT_IMPALING, "tridentImpaling", Rarity.RARE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 8 * level - 7
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 20
    }

    override val maxLevel: Int
        get() = 5

    override fun getDamageBonus(target: Entity, damager: Entity): Double {
        if (target.isTouchingWater() || (target.level!!.isRaining && target.level!!.canBlockSeeSky(target.position))) {
            return 2.5 * this.level
        }

        return 0.0
    }
}
