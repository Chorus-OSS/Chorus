package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class PlayerGlassBottleFillEvent(player: Player, target: Block, item: Item) :
    PlayerEvent(), Cancellable {
    val item: Item
    val block: Block

    init {
        this.player = player
        this.block = target
        this.item = item.clone()
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
