package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.BlockBell
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class BellRingEvent(bell: BlockBell, val cause: RingCause, val entity: Entity) : BlockEvent(bell), Cancellable {
    override val block: BlockBell
        get() {
            return super.block as BlockBell
        }

    enum class RingCause {
        HUMAN_INTERACTION,
        REDSTONE,
        PROJECTILE,
        DROPPED_ITEM,
        UNKNOWN
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
