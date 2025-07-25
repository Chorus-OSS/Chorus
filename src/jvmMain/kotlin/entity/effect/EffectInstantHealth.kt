package org.chorus_oss.chorus.entity.effect

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityRegainHealthEvent
import java.awt.Color

class EffectInstantHealth :
    InstantEffect(EffectType.INSTANT_HEALTH, "%potion.heal", Color(248, 36, 35)) {
    override fun apply(entity: Entity, tickCount: Double) {
        val amount: Double = (4 shl this.getAmplifier()) * tickCount
        if (entity.isUndead()) {
            entity.attack(EntityDamageEvent(entity, DamageCause.MAGIC, amount.toFloat()))
        } else {
            entity.heal(EntityRegainHealthEvent(entity, amount.toFloat(), EntityRegainHealthEvent.CAUSE_MAGIC))
        }
    }
}
