package org.chorus_oss.chorus.item.enchantment.protection

import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause


class EnchantmentProtectionAll :
    EnchantmentProtection(ID_PROTECTION_ALL, "all", Rarity.COMMON, TYPE.ALL) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 1 + (level - 1) * 11
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 11
    }

    override val typeModifier: Double
        get() = 1.0

    override fun getProtectionFactor(e: EntityDamageEvent): Float {
        val cause = e.cause

        if (level <= 0 || cause == DamageCause.VOID || cause == DamageCause.CUSTOM || cause == DamageCause.MAGIC || cause == DamageCause.HUNGER) {
            return 0f
        }

        return (this.level * typeModifier).toFloat()
    }
}
