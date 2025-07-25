package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Transform

class PlayerMoveEvent @JvmOverloads constructor(
    player: Player,
    from: Transform,
    to: Transform,
    resetBlocks: Boolean = true
) :
    PlayerEvent(), Cancellable {
    var from: Transform

    @JvmField
    var to: Transform

    var isResetBlocksAround: Boolean

    init {
        this.player = player
        this.from = from
        this.to = to
        this.isResetBlocksAround = resetBlocks
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
