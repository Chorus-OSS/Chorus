package org.chorus_oss.chorus.entity.effect

import org.chorus_oss.chorus.entity.Entity
import java.awt.Color

class EffectHealthBoost : Effect(EffectType.HEALTH_BOOST, "%potion.healthBoost", Color(248, 125, 35)) {
    override fun add(entity: Entity) {
        entity.setMaxHealth(entity.getMaxHealth() + 4 * this.getLevel())
    }

    override fun remove(entity: Entity) {
        entity.setMaxHealth(entity.getMaxHealth() - 4 * this.getLevel())
        if (entity.health > entity.getMaxHealth()) {
            entity.setHealthSafe(entity.getMaxHealth().toFloat())
        }
    }
}
