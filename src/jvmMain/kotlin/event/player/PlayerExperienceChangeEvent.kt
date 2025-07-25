package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerExperienceChangeEvent(player: Player, oldExp: Int, oldLevel: Int, newExp: Int, newLevel: Int) :
    PlayerEvent(), Cancellable {
    val oldExperience: Int
    val oldExperienceLevel: Int
    var newExperience: Int
    var newExperienceLevel: Int

    init {
        this.player = player
        this.oldExperience = oldExp
        this.oldExperienceLevel = oldLevel
        this.newExperience = newExp
        this.newExperienceLevel = newLevel
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
