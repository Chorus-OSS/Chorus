package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item


class BlockPlaceEvent(
    val player: Player,
    blockPlace: Block,
    val blockReplace: Block,
    val blockAgainst: Block,
    val item: Item
) :
    BlockEvent(blockPlace), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
