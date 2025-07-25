package org.chorus_oss.chorus.item.enchantment.protection

import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause


class EnchantmentProtectionFire :
    EnchantmentProtection(ID_PROTECTION_FIRE, "fire", Rarity.UNCOMMON, TYPE.FIRE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 10 + (level - 1) * 8
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 8
    }

    override val typeModifier: Double
        get() = 2.0

    override fun getProtectionFactor(e: EntityDamageEvent): Float {
        val cause = e.cause

        if (level <= 0 || (cause != DamageCause.LAVA && cause != DamageCause.FIRE && cause != DamageCause.FIRE_TICK)) {
            return 0f
        }

        return (this.level * typeModifier).toFloat()
    }
}
