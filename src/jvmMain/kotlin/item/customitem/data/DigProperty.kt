package org.chorus_oss.chorus.item.customitem.data

import org.chorus_oss.chorus.nbt.tag.CompoundTag

class DigProperty {
    var states: CompoundTag
    private var speed: Int? = null

    constructor() {
        this.states = CompoundTag()
    }

    constructor(states: CompoundTag, speed: Int) {
        this.states = states
        this.speed = speed
    }

    fun setSpeed(speed: Int) {
        this.speed = speed
    }

    fun getSpeed(): Int? {
        return speed
    }
}
