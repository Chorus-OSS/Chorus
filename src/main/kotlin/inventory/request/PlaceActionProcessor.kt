package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.inventory.CreativeOutputInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.inventory.SoleInventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseContainer
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseSlot
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.itemstack.request.action.PlaceRequestAction

class PlaceActionProcessor : ItemStackRequestActionProcessor<PlaceRequestAction> {
    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.PLACE

    override fun handle(action: PlaceRequestAction, player: Player, context: ItemStackRequestContext): ActionResponse? {
        val sourceSlotType = action!!.source.container.container
        val destinationSlotType = action.destination.container.container
        val source: Inventory = NetworkMapping.getInventory(player, sourceSlotType)
        val destination: Inventory = NetworkMapping.getInventory(player, destinationSlotType)
        val sourceSlot = source.fromNetworkSlot(action.source.slot.toInt())
        val sourceStackNetworkId = action.source.stackNetworkID
        val destinationSlot = destination.fromNetworkSlot(action.destination.slot.toInt())
        val destinationStackNetworkId = action.destination.stackNetworkID
        val count = action.count

        var sourItem = source.getItem(sourceSlot)
        if (sourItem.isNothing) {
            log.warn("transfer an air item is not allowed")
            return context.error()
        }
        if (validateStackNetworkId(sourItem.getNetId(), sourceStackNetworkId)) {
            log.warn("mismatch source stack network id!")
            return context.error()
        }
        if (sourItem.getCount() < count) {
            log.warn(
                "transfer an item that has not enough count is not allowed. Expected: {}, Actual: {}",
                sourItem.getCount(),
                count
            )
            return context.error()
        }

        if (context.has(CraftCreativeActionProcessor.Companion.CRAFT_CREATIVE_KEY) && (context.get<Any>(
                CraftCreativeActionProcessor.Companion.CRAFT_CREATIVE_KEY
            ) as Boolean)
        ) { //If the player takes an item from creative mode, the destination is overridden directly
            if (source is CreativeOutputInventory) {
                sourItem = sourItem.clone().autoAssignStackNetworkId()
                destination.setItem(destinationSlot, sourItem, false)
                return context.success(
                    listOf(
                        ItemStackResponseContainer(
                            mutableListOf(
                                ItemStackResponseSlot(
                                    destination.toNetworkSlot(destinationSlot),
                                    destination.toNetworkSlot(destinationSlot),
                                    sourItem.getCount(),
                                    sourItem.getNetId(),
                                    sourItem.customName,
                                    sourItem.damage
                                )
                            ),
                            action.destination.container
                        )
                    )
                )
            }
        }

        val destItem = destination.getItem(destinationSlot)
        if (!destItem.isNothing && !destItem.equals(sourItem, true, true)) {
            log.warn("transfer an item to a slot that has a different item is not allowed")
            return context.error()
        }
        if (validateStackNetworkId(destItem.getNetId(), destinationStackNetworkId)) {
            log.warn("mismatch destination stack network id!")
            return context.error()
        }
        if (destItem.getCount() + count > destItem.maxStackSize) {
            log.warn("destination stack size bigger than the max stack size!")
            return context.error()
        }

        val resultSourItem: Item
        val resultDestItem: Item
        val sendSource =
            false //Previous "!(source instanceof SoleInventory);", Not sending the source fixes the drag item distribution. Shouldn't cause any problems because inventory management is serversided.
        val sendDest = destination !is SoleInventory
        //first case：transfer all item
        if (sourItem.getCount() == count.toInt()) {
            source.clear(sourceSlot, sendSource)
            resultSourItem = source.getItem(sourceSlot)
            if (!destItem.isNothing) {
                //目标物品不为空，直接添加数量，目标物品网络堆栈id不变
                resultDestItem = destItem
                resultDestItem.setCount(destItem.getCount() + count)
                destination.setItem(destinationSlot, resultDestItem, sendDest)
            } else {
                //目标物品为空，直接移动原有堆栈到新位置，网络堆栈id使用源物品的网络堆栈id（相当于换个位置）
                if (source is CreativeOutputInventory) {
                    //HACK: 若是从CREATED_OUTPUT拿出的，需要服务端自行新建个网络堆栈id
                    sourItem = sourItem.clone().autoAssignStackNetworkId()
                }
                resultDestItem = sourItem
                destination.setItem(destinationSlot, resultDestItem, sendDest)
            }
        } else { //second case：transfer a part of item
            resultSourItem = sourItem
            resultSourItem.setCount(resultSourItem.getCount() - count)
            source.setItem(sourceSlot, resultSourItem, sendSource) //减少源库存数量
            if (!destItem.isNothing) { //目标物品不为空
                resultDestItem = destItem
                resultDestItem.setCount(destItem.getCount() + count) //增加目的库存数量
                destination.setItem(destinationSlot, resultDestItem, sendDest)
            } else { //目标物品为空，为分出来的子物品堆栈新建网络堆栈id
                resultDestItem = sourItem.clone().autoAssignStackNetworkId()
                resultDestItem.setCount(count.toInt())
                destination.setItem(destinationSlot, resultDestItem, sendDest)
            }
        }
        val destItemStackResponseSlot =
            ItemStackResponseContainer(
                mutableListOf(
                    ItemStackResponseSlot(
                        destination.toNetworkSlot(destinationSlot),
                        destination.toNetworkSlot(destinationSlot),
                        resultDestItem.getCount(),
                        resultDestItem.getNetId(),
                        resultDestItem.customName,
                        resultDestItem.damage
                    )
                ),
                action.destination.container
            )
        //CREATED_OUTPUT不需要发source响应
        return if (source is CreativeOutputInventory) {
            context.success(listOf(destItemStackResponseSlot))
        } else {
            context.success(
                listOf(
                    ItemStackResponseContainer(
                        mutableListOf(
                            ItemStackResponseSlot(
                                source.toNetworkSlot(sourceSlot),
                                source.toNetworkSlot(sourceSlot),
                                resultSourItem.getCount(),
                                resultSourItem.getNetId(),
                                resultSourItem.customName,
                                resultSourItem.damage
                            )
                        ),
                        action.source.container
                    ), destItemStackResponseSlot
                )
            )
        }
    }

    companion object : Loggable
}
