package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.inventory.CraftItemEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.toItem
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.tags.ItemTags
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.item.desciptor.DefaultItemDescriptor
import org.chorus_oss.protocol.types.item.desciptor.ItemTagItemDescriptor
import org.chorus_oss.protocol.types.itemstack.request.action.AutoCraftRecipeRequestAction


class CraftRecipeAutoProcessor : ItemStackRequestActionProcessor<AutoCraftRecipeRequestAction> {
    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.CRAFT_RECIPE_AUTO

    override fun handle(
        action: AutoCraftRecipeRequestAction,
        player: Player,
        context: ItemStackRequestContext
    ): ActionResponse? {
        val recipe = Registries.RECIPE.getRecipeByNetworkId(action.recipeNetworkId.toInt())

        val eventItems = action.ingredients.map { it.descriptor.toItem() }.toTypedArray()

        val craftItemEvent = CraftItemEvent(player, eventItems, recipe, 1)
        Server.instance.pluginManager.callEvent(craftItemEvent)
        if (craftItemEvent.cancelled) {
            return context.error()
        }

        var success = 0
        for (clientInputItem in eventItems) {
            for (serverExpect in action.ingredients) {
                var match = false

                val descriptor = serverExpect.descriptor
                when (descriptor) {
                    is ItemTagItemDescriptor -> match = ItemTags.getTagSet(clientInputItem.id).contains(descriptor.tag)
                    is DefaultItemDescriptor -> match =
                        clientInputItem.getNetId() == descriptor.networkID.toInt() && clientInputItem.meta == descriptor.metadataValue?.toInt()
                }
                if (match) {
                    success++
                    break
                }
            }
        }

        val matched = success == action.ingredients.size
        if (!matched) {
            log.warn(
                "Mismatched recipe! Network id: {},Recipe name: {},Recipe type: {}",
                action.recipeNetworkId,
                recipe.recipeId,
                recipe.type
            )
            return context.error()
        } else {
            context.put(CraftRecipeActionProcessor.Companion.RECIPE_DATA_KEY, recipe)
            val consumeActions = CraftRecipeActionProcessor.Companion.findAllConsumeActions(
                context.itemStackRequest.actions,
                context.currentActionIndex + 1
            )

            var consumeActionCountNeeded = 0
            for (item in eventItems) {
                if (!item.isNothing) {
                    consumeActionCountNeeded++
                }
            }
            if (consumeActions.size < consumeActionCountNeeded) {
                log.warn("Mismatched consume action count! Expected: " + consumeActionCountNeeded + ", Actual: " + consumeActions.size)
                return context.error()
            }
            if (recipe.results.size == 1) {
                val output: Item = recipe.results.first().clone()
                output.setCount(output.getCount() * action.timesCrafted)
                val createdOutput = player.creativeOutputInventory
                createdOutput.setItem(0, output.clone().autoAssignStackNetworkId(), false)
            }
        }
        return null
    }

    companion object : Loggable
}
