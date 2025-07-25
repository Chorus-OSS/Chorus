package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList


class ItemSpawnEvent(item: EntityItem) : EntityEvent(), Cancellable {
    override var entity: Entity = item

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
