package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.network.protocol.types.SpawnPointType

class PlayerRespawnEvent(player: Player, position: Pair<Locator?, SpawnPointType?>) :
    PlayerEvent() {
    var respawnPosition: Pair<Locator?, SpawnPointType?> //Respawn Position

    init {
        this.player = player
        this.respawnPosition = position
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
