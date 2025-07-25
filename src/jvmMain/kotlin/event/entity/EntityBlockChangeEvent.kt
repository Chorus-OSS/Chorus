package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class EntityBlockChangeEvent(entity: Entity, from: Block, to: Block) :
    EntityEvent(), Cancellable {
    val from: Block
    val to: Block

    init {
        this.entity = entity
        this.from = from
        this.to = to
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
