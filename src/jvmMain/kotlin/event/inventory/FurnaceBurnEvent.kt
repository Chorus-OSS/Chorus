package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.blockentity.BlockEntityFurnace
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.event.block.BlockEvent
import org.chorus_oss.chorus.item.Item


class FurnaceBurnEvent(val furnace: BlockEntityFurnace, val fuel: Item, var burnTime: Int) : BlockEvent(
    furnace.block
),
    Cancellable {
    var isBurning: Boolean = true

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
