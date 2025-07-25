package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerGameModeChangeEvent(player: Player, newGameMode: Int, newAdventureSettings: AdventureSettings) :
    PlayerEvent(), Cancellable {
    val newGamemode: Int

    @JvmField
    var newAdventureSettings: AdventureSettings

    init {
        this.player = player
        this.newGamemode = newGameMode
        this.newAdventureSettings = newAdventureSettings
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
