package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class FarmLandDecayEvent(val entity: Entity?, farm: Block) : BlockEvent(farm), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
