package org.chorus_oss.chorus.entity.effect

import java.awt.Color

class EffectVillageHero : Effect(EffectType.VILLAGE_HERO, "%effect.villageHero", Color(68, 255, 68)) {
    init {
        this.setVisible(false)
    }
}
