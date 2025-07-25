package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityLectern
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item

class LecternPlaceBookEvent(val player: Player, val lectern: BlockEntityLectern, private var book: Item) :
    BlockEvent(lectern.block), Cancellable {
    fun getBook(): Item {
        return book.clone()
    }

    fun setBook(book: Item) {
        this.book = book
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
