package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseContainer
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseSlot
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.itemstack.request.action.ConsumeRequestAction

class ConsumeActionProcessor : ItemStackRequestActionProcessor<ConsumeRequestAction> {
    override fun handle(
        action: ConsumeRequestAction,
        player: Player,
        context: ItemStackRequestContext
    ): ActionResponse? {
        // We have validated the recipe in CraftRecipeActionProcessor, so here we can believe the client directly
        val count = action.count
        if (count == 0.toByte()) {
            log.warn("cannot consume 0 items!")

            return context.error()
        }

        val sourceContainer: Inventory = NetworkMapping.getInventory(player, action.source.container.container)
        val slot = sourceContainer.fromNetworkSlot(action.source.slot.toInt())
        var item = sourceContainer.getItem(slot)
        if (validateStackNetworkId(item.getNetId(), action.source.stackNetworkID)) {
            log.warn("mismatch stack network id!")

            return context.error()
        }

        if (item.isNothing) {
            log.warn("cannot consume an air!")

            return context.error()
        }

        if (item.getCount() < count) {
            log.warn("cannot consume more items than the current amount!")

            return context.error()
        }

        if (item.getCount() > count) {
            item.setCount(item.getCount() - count)
            sourceContainer.setItem(slot, item, false)
        } else {
            sourceContainer.clear(slot, false)
            item = sourceContainer.getItem(slot)
        }

        val isEnchRecipe = context.get<Boolean>(CraftRecipeActionProcessor.ENCH_RECIPE_KEY)
        if (isEnchRecipe != null && isEnchRecipe && action.source.container.container == org.chorus_oss.protocol.types.itemstack.ContainerSlotType.EnchantingInput) {
            return null
        }

        return context.success(
            listOf(
                ItemStackResponseContainer(
                    mutableListOf(
                        ItemStackResponseSlot(
                            sourceContainer.toNetworkSlot(slot),
                            sourceContainer.toNetworkSlot(slot),
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

    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.CONSUME

    companion object : Loggable
}
