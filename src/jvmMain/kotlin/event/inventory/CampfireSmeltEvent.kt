package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.blockentity.BlockEntityCampfire
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.event.block.BlockEvent
import org.chorus_oss.chorus.item.Item


class CampfireSmeltEvent(campfire: BlockEntityCampfire, source: Item, result: Item) :
    BlockEvent(campfire.block), Cancellable {
    val campfire: BlockEntityCampfire
    val source: Item = source.clone()
    var result: Item
    var keepItem: Boolean = false

    init {
        this.source.setCount(1)
        this.result = result
        this.campfire = campfire
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
