package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.itemstack.request.action.CraftRecipeRequestAction
import org.chorus_oss.protocol.types.itemstack.request.action.CreateRequestAction

class CreateActionProcessor : ItemStackRequestActionProcessor<CreateRequestAction> {
    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.CREATE

    override fun handle(
        action: CreateRequestAction,
        player: Player,
        context: ItemStackRequestContext
    ): ActionResponse? {
        val itemStackRequestAction = context.itemStackRequest.actions.firstOrNull { it is CraftRecipeRequestAction }
        if (itemStackRequestAction == null) {
            log.warn("Recipe not found in ItemStackRequest Context! Context: $context")
            return context.error()
        }
        val recipe =
            Registries.RECIPE.getRecipeByNetworkId((itemStackRequestAction as CraftRecipeRequestAction).recipeNetworkId.toInt())
        val output = recipe.results[action.resultsSlot.toInt()]
        val createdOutput = player.creativeOutputInventory
        createdOutput.setItem(0, output, false)
        return null
    }

    companion object : Loggable
}
