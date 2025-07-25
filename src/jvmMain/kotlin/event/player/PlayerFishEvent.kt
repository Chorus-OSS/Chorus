package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.item.EntityFishingHook
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.Vector3

class PlayerFishEvent(player: Player, hook: EntityFishingHook, loot: Item, experience: Int, motion: Vector3) :
    PlayerEvent(), Cancellable {
    val hook: EntityFishingHook
    var loot: Item
    var experience: Int
    var motion: Vector3

    init {
        this.player = player
        this.hook = hook
        this.loot = loot
        this.experience = experience
        this.motion = motion
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
