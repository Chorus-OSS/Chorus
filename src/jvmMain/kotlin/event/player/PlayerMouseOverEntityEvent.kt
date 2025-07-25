package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.HandlerList

class PlayerMouseOverEntityEvent(player: Player, entity: Entity) : PlayerEvent() {
    val entity: Entity

    init {
        this.player = player
        this.entity = entity
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
