package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.recipe.Recipe
import org.chorus_oss.chorus.recipe.RecipeType
import org.chorus_oss.protocol.types.itemstack.request.action.CraftResultsDeprecatedRequestAction

class CraftResultDeprecatedActionProcessor : ItemStackRequestActionProcessor<CraftResultsDeprecatedRequestAction> {
    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.CRAFT_RESULTS_DEPRECATED

    override fun handle(
        action: CraftResultsDeprecatedRequestAction,
        player: Player,
        context: ItemStackRequestContext
    ): ActionResponse? {
        if (context.has(CraftRecipeActionProcessor.Companion.RECIPE_DATA_KEY) && (context.get<Any>(
                CraftRecipeActionProcessor.Companion.RECIPE_DATA_KEY
            ) as Recipe).type == RecipeType.MULTI
        ) {
            val createdOutput = player.creativeOutputInventory
            val resultItem = Item(action.resultItems[0])
            resultItem.autoAssignStackNetworkId()
            createdOutput.setItem(0, resultItem, false)
            return null
        }
        context.put(NO_RESPONSE_DESTROY_KEY, true)
        return null
    }

    companion object {
        const val NO_RESPONSE_DESTROY_KEY: String = "noResponseForDestroyAction"
    }
}
