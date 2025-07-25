package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class EntityInteractEvent(entity: Entity, block: Block) : EntityEvent(), Cancellable {
    val block: Block

    init {
        this.entity = entity
        this.block = block
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
