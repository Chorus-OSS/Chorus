package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerDropItemEvent
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseContainer
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseSlot
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.itemstack.request.action.DropRequestAction

class DropActionProcessor : ItemStackRequestActionProcessor<DropRequestAction> {
    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.DROP

    override fun handle(action: DropRequestAction, player: Player, context: ItemStackRequestContext): ActionResponse {
        val inventory: Inventory = NetworkMapping.getInventory(player, action.source.container.container)
        val count = action.count
        val slot = inventory.fromNetworkSlot(action.source.slot.toInt())
        var item = inventory.getItem(slot)

        val ev: PlayerDropItemEvent
        Server.instance.pluginManager.callEvent(PlayerDropItemEvent(player, item).also { ev = it })
        if (ev.cancelled) {
            return context.error()
        }

        if (validateStackNetworkId(item.getNetId(), action.source.stackNetworkID)) {
            log.warn("mismatch stack network id!")
            return context.error()
        }
        if (item.isNothing) {
            log.warn("cannot throw an air!")
            return context.error()
        }
        if (item.getCount() < count) {
            log.warn("cannot throw more items than the current amount!")
            return context.error()
        }
        val drop = item.clone()
        drop.setCount(count.toInt())
        player.dropItem(drop)

        val c = item.getCount() - count
        if (c <= 0) {
            inventory.clear(slot, false)
            item = inventory.getItem(slot)
        } else {
            item.setCount(c)
            inventory.setItem(slot, item, false)
        }
        return context.success(
            listOf(
                ItemStackResponseContainer(
                    mutableListOf(
                        ItemStackResponseSlot(
                            inventory.toNetworkSlot(slot),
                            inventory.toNetworkSlot(slot),
                            item.getCount(),
                            item.getNetId(),
                            item.customName,
                            item.damage
                        )
                    ),
                    action.source.container
                )
            )
        )
    }

    companion object : Loggable
}
