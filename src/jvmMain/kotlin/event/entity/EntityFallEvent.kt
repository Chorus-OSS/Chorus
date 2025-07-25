package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class EntityFallEvent(entity: Entity, blockFallOn: Block, fallDistance: Float) :
    EntityEvent(), Cancellable {
    val blockFallOn: Block
    var fallDistance: Float

    init {
        this.entity = entity
        this.blockFallOn = blockFallOn
        this.fallDistance = fallDistance
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
