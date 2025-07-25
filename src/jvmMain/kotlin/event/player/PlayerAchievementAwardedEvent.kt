package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerAchievementAwardedEvent(player: Player, achievementId: String) : PlayerEvent(), Cancellable {
    val achievement: String

    init {
        this.player = player
        this.achievement = achievementId
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
