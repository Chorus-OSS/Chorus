package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.inventory.ItemStackRequestActionEvent
import org.chorus_oss.chorus.event.player.PlayerTransferItemEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.inventory.request.*
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.network.protocol.types.itemstack.response.ItemStackResponseContainer
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.ItemStackRequestPacket
import org.chorus_oss.protocol.types.itemstack.request.action.*


class ItemStackRequestPacketProcessor : DataPacketProcessor<MigrationPacket<ItemStackRequestPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<ItemStackRequestPacket>) {
        val packet = pk.packet

        val player = player.player
        val responses: MutableList<org.chorus_oss.protocol.types.itemstack.response.ItemStackResponse> = mutableListOf()
        for (request in packet.requests) {
            val actions = request.actions
            val context = ItemStackRequestContext(request)
            val responseContainerMap: MutableMap<org.chorus_oss.protocol.types.itemstack.ContainerSlotType, ItemStackResponseContainer> =
                LinkedHashMap()
            for (index in actions.indices) {
                val action = actions[index]
                context.currentActionIndex = (index)
                @Suppress("UNCHECKED_CAST")
                val processor =
                    PROCESSORS[action.type] as ItemStackRequestActionProcessor<ItemStackRequestAction>?
                if (processor == null) {
                    log.warn("Unhandled inventory action type {}", action.type)
                    continue
                }

                val event = ItemStackRequestActionEvent(player, action, context)
                TransferItemEventCaller.call(event)
                Server.instance.pluginManager.callEvent(event)

                val response = if (event.response != null) {
                    event.response
                } else if (event.cancelled) {
                    context.error()
                } else {
                    processor.handle(action, player, context)
                }
                if (response != null) {
                    if (!response.ok) {
                        val itemStackResponse = org.chorus_oss.protocol.types.itemstack.response.ItemStackResponse(
                            org.chorus_oss.protocol.types.itemstack.response.ItemStackResponseStatus.Error,
                            request.requestId,
                            listOf()
                        )
                        responses.add(itemStackResponse)
                        break
                    }

                    for (container in response.containers) {
                        responseContainerMap.compute(
                            container.containerName.container
                        ) { key, oldValue ->
                            if (oldValue == null) {
                                return@compute container
                            } else {
                                oldValue.items.addAll(container.items)
                                return@compute oldValue
                            }
                        }
                    }
                }
            }
            val itemStackResponse = org.chorus_oss.protocol.types.itemstack.response.ItemStackResponse(
                result = org.chorus_oss.protocol.types.itemstack.response.ItemStackResponseStatus.OK,
                requestID = request.requestId,
                containers = responseContainerMap.values.map(org.chorus_oss.protocol.types.itemstack.response.ItemStackResponseContainer::invoke)
            )
            responses.add(itemStackResponse)
        }

        val itemStackResponsePacket = org.chorus_oss.protocol.packets.ItemStackResponsePacket(
            responses = responses
        )
        player.sendPacket(itemStackResponsePacket)
    }

    override val packetId: Int = ItemStackRequestPacket.id

    private object TransferItemEventCaller {
        fun call(event: ItemStackRequestActionEvent) {
            val action = event.action

            val transferResult = handleAction(action) ?: return

            val player = event.player
            val sourceInventory = NetworkMapping.getInventory(player, transferResult.source.container.container)
            val sourceSlot = sourceInventory.fromNetworkSlot(transferResult.source.slot.toInt())

            val destinationInventory = transferResult.destination?.let { destination ->
                NetworkMapping.getInventory(
                    player,
                    destination.container.container
                )
            }
            val destinationSlot = destinationInventory?.let { inventory ->
                transferResult.destination.let { destination -> inventory.fromNetworkSlot(destination.slot.toInt()) }
            }

            val destinationItem = destinationSlot?.let { slot: Int? ->
                destinationInventory.let { inventory: Inventory? ->
                    inventory!!.getItem(slot!!)
                }
            }

            val transferEvent = PlayerTransferItemEvent(
                player,
                transferResult.type,
                sourceInventory.getItem(sourceSlot),
                destinationItem,
                sourceSlot,
                destinationSlot ?: -1,
                sourceInventory,
                destinationInventory
            )

            Server.instance.pluginManager.callEvent(transferEvent)
            if (transferEvent.cancelled) {
                event.cancelled = true
            }
        }


        fun handleAction(action: ItemStackRequestAction): TransferResult? {
            return when (action) {
                is PlaceRequestAction -> TransferResult(
                    action.source,
                    action.destination,
                    PlayerTransferItemEvent.Type.TRANSFER
                )

                is TakeRequestAction -> TransferResult(
                    action.source,
                    action.destination,
                    PlayerTransferItemEvent.Type.TRANSFER
                )

                is SwapRequestAction -> TransferResult(
                    action.source,
                    action.destination,
                    PlayerTransferItemEvent.Type.SWAP
                )

                is DropRequestAction -> TransferResult(
                    action.source,
                    null,
                    PlayerTransferItemEvent.Type.DROP
                )

                else -> null
            }
        }


        @JvmRecord
        private data class TransferResult(
            val source: org.chorus_oss.protocol.types.itemstack.request.ItemStackRequestSlotData,
            val destination: org.chorus_oss.protocol.types.itemstack.request.ItemStackRequestSlotData?,
            val type: PlayerTransferItemEvent.Type
        )
    }

    companion object : Loggable {
        val PROCESSORS: Map<ItemStackRequestAction.Companion.Type, ItemStackRequestActionProcessor<*>> = mapOf(
            ItemStackRequestAction.Companion.Type.Consume to ConsumeActionProcessor(),
            ItemStackRequestAction.Companion.Type.CraftCreative to CraftCreativeActionProcessor(),
            ItemStackRequestAction.Companion.Type.CraftRecipe to CraftRecipeActionProcessor(),
            ItemStackRequestAction.Companion.Type.CraftResultsDeprecated to CraftResultDeprecatedActionProcessor(),
            ItemStackRequestAction.Companion.Type.CraftRecipeAuto to CraftRecipeAutoProcessor(),
            ItemStackRequestAction.Companion.Type.Create to CreateActionProcessor(),
            ItemStackRequestAction.Companion.Type.Destroy to DestroyActionProcessor(),
            ItemStackRequestAction.Companion.Type.Drop to DropActionProcessor(),
            ItemStackRequestAction.Companion.Type.Place to PlaceActionProcessor(),
            ItemStackRequestAction.Companion.Type.Swap to SwapActionProcessor(),
            ItemStackRequestAction.Companion.Type.Take to TakeActionProcessor(),
            ItemStackRequestAction.Companion.Type.CraftRecipeOptional to CraftRecipeOptionalProcessor(),
            ItemStackRequestAction.Companion.Type.CraftRepairAndDisenchant to CraftGrindstoneActionProcessor(),
            ItemStackRequestAction.Companion.Type.MineBlock to MineBlockActionProcessor(),
            ItemStackRequestAction.Companion.Type.CraftLoom to CraftLoomActionProcessor(),
            ItemStackRequestAction.Companion.Type.BeaconPayment to BeaconPaymentActionProcessor(),
        )
    }
}
