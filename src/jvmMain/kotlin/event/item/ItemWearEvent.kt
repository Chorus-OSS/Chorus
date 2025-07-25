package org.chorus_oss.chorus.event.item

import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class ItemWearEvent(item: Item, @JvmField var newDurability: Int) : ItemEvent(item), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
