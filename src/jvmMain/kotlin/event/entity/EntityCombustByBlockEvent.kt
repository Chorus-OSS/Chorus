package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.HandlerList

class EntityCombustByBlockEvent(val block: Block, entity: Entity, duration: Int) :
    EntityCombustEvent(entity, duration) {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
