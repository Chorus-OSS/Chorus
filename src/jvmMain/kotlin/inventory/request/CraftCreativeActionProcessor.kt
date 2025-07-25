package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.itemstack.request.action.CraftCreativeRequestAction

class CraftCreativeActionProcessor : ItemStackRequestActionProcessor<CraftCreativeRequestAction> {
    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.CRAFT_CREATIVE

    override fun handle(
        action: CraftCreativeRequestAction,
        player: Player,
        context: ItemStackRequestContext
    ): ActionResponse? {
        var item = Registries.CREATIVE[action.creativeItemNetworkId.toInt()]
        if (!player.isCreative) {
            log.warn(
                "This player {} is get createitems in non-creative mode, which may be a hacker!",
                player.getEntityName()
            )
            return context.error()
        }
        if (item == null) {
            log.warn("Unknown creative item network id: {}", action.creativeItemNetworkId)
            return context.error()
        }
        item = item.clone().autoAssignStackNetworkId()
        item.setCount(item.maxStackSize)
        player.creativeOutputInventory.setItem(item)
        //Picking up something from the creation inventory does not require a response
        context.put(CRAFT_CREATIVE_KEY, true)
        return null
    }

    companion object : Loggable {
        const val CRAFT_CREATIVE_KEY: String = "craft_creative_key"
    }
}
