package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class BlockIgniteEvent(block: Block, val source: Block?, val entity: Entity?, val cause: BlockIgniteCause) :
    BlockEvent(block), Cancellable {
    enum class BlockIgniteCause {
        EXPLOSION,
        FIREBALL,
        FLINT_AND_STEEL,
        LAVA,
        LIGHTNING,
        SPREAD
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
