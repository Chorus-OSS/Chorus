package org.chorus_oss.chorus.event.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityLectern
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class LecternPageChangeEvent(val player: Player, val lectern: BlockEntityLectern, @JvmField var newRawPage: Int) :
    BlockEvent(
        lectern.block
    ),
    Cancellable {
    var leftPage: Int
        get() = (newRawPage * 2) + 1
        set(newLeftPage) {
            this.newRawPage = (newLeftPage - 1) / 2
        }

    var rightPage: Int
        get() = leftPage + 1
        set(newRightPage) {
            this.leftPage = newRightPage - 1
        }

    val maxPage: Int
        get() = lectern.totalPages

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
