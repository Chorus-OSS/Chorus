package org.chorus_oss.chorus.item.enchantment.protection

import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause


class EnchantmentProtectionProjectile : EnchantmentProtection(
    ID_PROTECTION_PROJECTILE,
    "projectile",
    Rarity.UNCOMMON,
    TYPE.PROJECTILE
) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 3 + (level - 1) * 6
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 6
    }

    override val typeModifier: Double
        get() = 3.0

    override fun getProtectionFactor(e: EntityDamageEvent): Float {
        val cause = e.cause

        if (level <= 0 || (cause != DamageCause.PROJECTILE)) {
            return 0f
        }

        return (this.level * typeModifier).toFloat()
    }
}
