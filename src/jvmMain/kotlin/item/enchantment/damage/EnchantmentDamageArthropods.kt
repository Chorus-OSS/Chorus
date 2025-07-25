package org.chorus_oss.chorus.item.enchantment.damage

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityArthropod
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import java.util.concurrent.ThreadLocalRandom


class EnchantmentDamageArthropods :
    EnchantmentDamage(ID_DAMAGE_ARTHROPODS, "arthropods", Rarity.UNCOMMON, TYPE.SMITE) {
    override fun getMinEnchantAbility(level: Int): Int {
        return 5 + (level - 1) * 8
    }

    override fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 20
    }

    override fun getDamageBonus(target: Entity, damager: Entity): Double {
        if (target is EntityArthropod) {
            return this.level * 2.5
        }

        return 0.0
    }

    override fun doAttack(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity is EntityArthropod) {
            val duration = 20 + ThreadLocalRandom.current().nextInt(10 * this.level)
            entity.addEffect(Effect.get(EffectType.SLOWNESS).setDuration(duration).setAmplifier(3))
        }
    }
}
