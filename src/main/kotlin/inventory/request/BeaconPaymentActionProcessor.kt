package org.chorus_oss.chorus.inventory.request

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityBeacon
import org.chorus_oss.chorus.inventory.BeaconInventory
import org.chorus_oss.chorus.network.protocol.types.itemstack.request.action.ItemStackRequestActionType
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.itemstack.request.action.BeaconPaymentRequestAction


class BeaconPaymentActionProcessor : ItemStackRequestActionProcessor<BeaconPaymentRequestAction>, Loggable {
    override fun handle(
        action: BeaconPaymentRequestAction,
        player: Player,
        context: ItemStackRequestContext
    ): ActionResponse? {
        val topWindow = player.topWindow
        if (topWindow.isEmpty) {
            log.error("the player's haven't open any inventory!")
            return context.error()
        }
        val beaconInventory = topWindow.get()
        if (beaconInventory !is BeaconInventory) {
            log.error("the player's haven't open beacon inventory!")
            return context.error()
        }

        val holder: BlockEntityBeacon = beaconInventory.holder as BlockEntityBeacon
        holder.primaryPower = action.primaryEffect
        holder.secondaryPower = action.secondaryEffect
        return null
    }

    override val type: ItemStackRequestActionType
        get() = ItemStackRequestActionType.BEACON_PAYMENT
}
