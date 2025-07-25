package org.chorus_oss.chorus.entity.effect

import java.awt.Color

open class InstantEffect : Effect {
    constructor(type: EffectType, name: String, color: Color) : super(type, name, color) {
        this.setDuration(1)
    }

    constructor(type: EffectType, name: String, color: Color, bad: Boolean) : super(type, name, color, bad) {
        this.setDuration(1)
    }

    override fun canTick(): Boolean {
        return true
    }
}
