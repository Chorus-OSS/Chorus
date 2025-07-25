package org.chorus_oss.chorus.entity.effect

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityLiving
import java.awt.Color

class EffectSpeed :
    Effect(EffectType.SPEED, "%potion.moveSpeed", Color(51, 235, 255)) {
    override fun add(entity: Entity) {
        if (entity is EntityLiving) {
            val oldEffect: Effect? = entity.getEffect(this.getType())
            if (oldEffect != null) {
                entity.setMovementSpeedF(entity.movementSpeed / (1 + 0.2f * oldEffect.getLevel()))
            }

            entity.setMovementSpeedF(entity.movementSpeed * (1 + 0.2f * this.getLevel()))
        }
    }

    override fun remove(entity: Entity) {
        if (entity is EntityLiving) {
            entity.setMovementSpeedF(entity.movementSpeed / (1 + 0.2f * this.getLevel()))
        }
    }
}
