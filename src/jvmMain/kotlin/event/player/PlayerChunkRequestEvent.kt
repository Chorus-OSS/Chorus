package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.HandlerList

class PlayerChunkRequestEvent(player: Player, chunkX: Int, chunkZ: Int) : PlayerEvent() {
    val chunkX: Int
    val chunkZ: Int

    init {
        this.player = player
        this.chunkX = chunkX
        this.chunkZ = chunkZ
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
