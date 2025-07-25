package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseContainer
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseSlot
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.itemstack.request.action.SwapRequestAction

class SwapActionProcessor : ItemStackRequestActionProcessor<SwapRequestAction> {
    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.SWAP

    override fun handle(action: SwapRequestAction, player: Player, context: ItemStackRequestContext): ActionResponse {
        val sourceSlotType = action.source.container.container
        val destinationSlotType = action.destination.container.container
        val source: Inventory = NetworkMapping.getInventory(player, sourceSlotType)
        val destination: Inventory = NetworkMapping.getInventory(player, destinationSlotType)

        val sourceSlot = source.fromNetworkSlot(action.source.slot.toInt())
        val destinationSlot = destination.fromNetworkSlot(action.destination.slot.toInt())
        val sourceItem = source.getItem(sourceSlot)
        val destinationItem = destination.getItem(destinationSlot)
        if (validateStackNetworkId(sourceItem.getNetId(), action.source.stackNetworkID)) {
            log.warn("mismatch stack network id!")
            return context.error()
        }
        if (validateStackNetworkId(destinationItem.getNetId(), action.destination.stackNetworkID)) {
            log.warn("mismatch stack network id!")
            return context.error()
        }
        source.setItem(sourceSlot, destinationItem, false)
        destination.setItem(destinationSlot, sourceItem, false)
        return context.success(
            listOf(
                ItemStackResponseContainer(
                    mutableListOf(
                        ItemStackResponseSlot(
                            source.toNetworkSlot(sourceSlot),
                            source.toNetworkSlot(sourceSlot),
                            destinationItem.getCount(),
                            destinationItem.getNetId(),
                            destinationItem.customName,
                            destinationItem.damage
                        )
                    ),
                    action.source.container
                ),
                ItemStackResponseContainer(
                    mutableListOf(
                        ItemStackResponseSlot(
                            destination.toNetworkSlot(destinationSlot),
                            destination.toNetworkSlot(destinationSlot),
                            sourceItem.getCount(),
                            sourceItem.getNetId(),
                            sourceItem.customName,
                            sourceItem.damage
                        )
                    ),
                    action.destination.container
                )
            )
        )
    }

    companion object : Loggable
}
