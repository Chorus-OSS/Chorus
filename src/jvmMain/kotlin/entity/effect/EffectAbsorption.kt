package org.chorus_oss.chorus.entity.effect

import org.chorus_oss.chorus.entity.Entity
import java.awt.Color

class EffectAbsorption :
    Effect(EffectType.ABSORPTION, "%potion.absorption", Color(37, 82, 165)) {
    override fun add(entity: Entity) {
        val absorption: Int = (4 * this.getLevel())
        if (absorption > entity.getAbsorption()) {
            entity.setAbsorption(absorption.toFloat())
        }
    }

    override fun remove(entity: Entity) {
        entity.setAbsorption(0f)
    }
}
