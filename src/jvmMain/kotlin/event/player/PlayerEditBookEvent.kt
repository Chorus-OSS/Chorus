package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class PlayerEditBookEvent(
    player: Player,
    val oldBook: Item,
    var newBook: Item,
    val action: org.chorus_oss.protocol.packets.BookEditPacket.Action
) :
    PlayerEvent(), Cancellable {

    init {
        this.player = player
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
