package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.inventory.request.ActionResponse
import org.chorus_oss.chorus.inventory.request.ItemStackRequestContext

class ItemStackRequestActionEvent(
    @JvmField val player: Player,
    @JvmField val action: org.chorus_oss.protocol.types.itemstack.request.action.ItemStackRequestAction,
    val context: ItemStackRequestContext
) :
    Event(), Cancellable {
    @JvmField
    var response: ActionResponse? = null

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
