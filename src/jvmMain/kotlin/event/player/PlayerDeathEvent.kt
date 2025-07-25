package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.event.entity.EntityDeathEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.lang.TextContainer
import org.chorus_oss.chorus.lang.TranslationContainer

class PlayerDeathEvent(
    player: Player,
    drops: Array<Item>,
    @JvmField var deathMessage: TextContainer,
    @JvmField var experience: Int
) :
    EntityDeathEvent(player, drops), Cancellable {
    @JvmField
    var keepInventory: Boolean = false

    @JvmField
    var keepExperience: Boolean = false

    constructor(player: Player, drops: Array<Item>, deathMessage: String, experience: Int) : this(
        player,
        drops,
        TextContainer(deathMessage),
        experience
    )

    val translationDeathMessage: TranslationContainer
        get() = if (deathMessage is TranslationContainer) deathMessage as TranslationContainer else TranslationContainer(
            deathMessage.text
        )

    fun setDeathMessage(deathMessage: TranslationContainer) {
        this.deathMessage = deathMessage
    }

    fun setDeathMessage(deathMessage: TextContainer) {
        this.deathMessage = deathMessage
    }

    fun setDeathMessage(deathMessage: String) {
        this.deathMessage = TextContainer(deathMessage)
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
