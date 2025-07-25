package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.property.enums.CauldronLiquid
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class CauldronFilledByDrippingLiquidEvent(
    cauldron: Block,
    @JvmField var liquid: CauldronLiquid,
    @JvmField var liquidLevelIncrement: Int
) :
    BlockEvent(cauldron), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
