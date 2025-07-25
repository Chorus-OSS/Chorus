package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList


class EntityRegainHealthEvent(entity: Entity, amount: Float, regainReason: Int) :
    EntityEvent(), Cancellable {
    var amount: Float
    val regainReason: Int

    init {
        this.entity = entity
        this.amount = amount
        this.regainReason = regainReason
    }

    companion object {
        val handlers: HandlerList = HandlerList()

        const val CAUSE_REGEN: Int = 0
        const val CAUSE_EATING: Int = 1
        const val CAUSE_MAGIC: Int = 2
        const val CAUSE_CUSTOM: Int = 3
    }
}
