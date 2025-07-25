package org.chorus_oss.chorus.entity.effect

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityRegainHealthEvent
import java.awt.Color

class EffectInstantDamage :
    InstantEffect(EffectType.INSTANT_DAMAGE, "%potion.harm", Color(169, 101, 106), true) {
    override fun apply(entity: Entity, tickCount: Double) {
        val amount: Double = (6 shl this.getAmplifier()) * tickCount
        if (entity.isUndead()) {
            entity.heal(EntityRegainHealthEvent(entity, amount.toFloat(), EntityRegainHealthEvent.CAUSE_MAGIC))
        } else {
            entity.attack(EntityDamageEvent(entity, DamageCause.MAGIC, amount.toFloat()))
        }
    }
}
