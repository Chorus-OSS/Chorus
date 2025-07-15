package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.inventory.SpecialWindowId
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseContainer
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseSlot
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.item.ItemStack
import org.chorus_oss.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.types.itemstack.request.action.MineBlockRequestAction


class MineBlockActionProcessor : ItemStackRequestActionProcessor<MineBlockRequestAction> {
    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.MINE_BLOCK

    override fun handle(
        action: MineBlockRequestAction,
        player: Player,
        context: ItemStackRequestContext
    ): ActionResponse {
        val inventory = player.inventory
        val heldItemIndex = inventory.heldItemIndex
        if (heldItemIndex != action.hotbarSlot) {
            log.warn("The held Item Index on the server side does not match the client side!")
            return context.error()
        }

        val itemInHand = inventory.itemInHand
        if (validateStackNetworkId(itemInHand.getNetId(), action.stackNetworkID)) {
            log.warn("mismatch source stack network id!")
            return context.error()
        }

        if (itemInHand.damage != action.predictedDurability) {
            val id = SpecialWindowId.PLAYER.id
            val packet = org.chorus_oss.protocol.packets.InventorySlotPacket(
                windowID = id.toUInt(),
                slot = action.hotbarSlot.toUInt(),
                container = org.chorus_oss.protocol.types.inventory.FullContainerName(
                    ContainerSlotType.Hotbar,
                    id,
                ),
                storageItem = ItemStack(Item.AIR),
                newItem = ItemStack(itemInHand),
            )
            player.sendPacket(packet)
        }
        val itemStackResponseSlot =
            ItemStackResponseContainer(
                mutableListOf(
                    ItemStackResponseSlot(
                        inventory.toNetworkSlot(heldItemIndex),
                        inventory.toNetworkSlot(heldItemIndex),
                        itemInHand.getCount(),
                        itemInHand.getNetId(),
                        itemInHand.customName,
                        itemInHand.damage
                    )
                ),
                org.chorus_oss.protocol.types.inventory.FullContainerName(
                    ContainerSlotType.entries[(inventory.getSlotType(heldItemIndex)).ordinal],
                    0 // I don't know the purpose of the dynamicId yet, this is why I leave it at 0 for the MineBlockAction
                )
            )
        return context.success(listOf(itemStackResponseSlot))
    }

    companion object : Loggable
}
