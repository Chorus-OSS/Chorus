package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.property.enums.BigDripleafTilt
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class BigDripleafTiltChangeEvent(block: Block, val oldTilt: BigDripleafTilt, var newTilt: BigDripleafTilt) :
    BlockEvent(block), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
