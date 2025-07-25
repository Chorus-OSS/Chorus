package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.block.BlockHopper.IHopper
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.HandlerList

class HopperSearchItemEvent(val hopper: IHopper, val isMinecart: Boolean) : Event(), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
