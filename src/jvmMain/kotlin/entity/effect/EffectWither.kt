package org.chorus_oss.chorus.entity.effect

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import java.awt.Color

class EffectWither :
    Effect(EffectType.WITHER, "%potion.wither", Color(115, 97, 86), true) {
    override fun canTick(): Boolean {
        val interval: Int = 25 shr this.getAmplifier()
        return interval > 0 && this.getDuration() % interval == 0
    }

    override fun apply(entity: Entity, tickCount: Double) {
        entity.attack(EntityDamageEvent(entity, DamageCause.MAGIC, 1f))
    }
}
