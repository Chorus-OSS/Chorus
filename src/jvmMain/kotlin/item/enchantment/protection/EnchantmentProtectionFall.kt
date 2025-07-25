package org.chorus_oss.chorus.item.enchantment.protection

import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause


class EnchantmentProtectionFall :
    EnchantmentProtection(ID_PROTECTION_FALL, "fall", Rarity.UNCOMMON, TYPE.FALL) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 5 + (level - 1) * 6
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 6
    }

    override val typeModifier: Double
        get() = 2.0

    override fun getProtectionFactor(e: EntityDamageEvent): Float {
        val cause = e.cause

        if (level <= 0 || (cause != DamageCause.FALL)) {
            return 0f
        }

        return (this.level * typeModifier).toFloat()
    }
}
