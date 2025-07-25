package org.chorus_oss.chorus.item.enchantment.protection

import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause


class EnchantmentProtectionExplosion :
    EnchantmentProtection(ID_PROTECTION_EXPLOSION, "explosion", Rarity.RARE, TYPE.EXPLOSION) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 5 + (level - 1) * 8
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 8
    }

    override val typeModifier: Double
        get() = 2.0

    override fun getProtectionFactor(e: EntityDamageEvent): Float {
        val cause = e.cause

        if (level <= 0 || (cause != DamageCause.ENTITY_EXPLOSION && cause != DamageCause.BLOCK_EXPLOSION)) {
            return 0f
        }

        return (this.level * typeModifier).toFloat()
    }
}
