package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.experimental.network.protocol.utils.EnchantmentOptionData
import org.chorus_oss.chorus.inventory.EnchantInventory

class PlayerEnchantOptionsRequestEvent(player: Player, val inventory: EnchantInventory, var options: List<EnchantmentOptionData>) :
    PlayerEvent(), Cancellable {

    init { this.player = player }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
