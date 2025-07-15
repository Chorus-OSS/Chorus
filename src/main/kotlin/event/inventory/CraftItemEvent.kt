package org.chorus_oss.chorus.event.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.recipe.Recipe

class CraftItemEvent(val player: Player, val input: Array<Item>, val recipe: Recipe, val count: Byte) :
    Event(), Cancellable {

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}