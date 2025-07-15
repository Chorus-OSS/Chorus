package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.inventory.SpecialWindowId
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.types.ContainerType

class ContainerCloseProcessor :
    DataPacketProcessor<MigrationPacket<org.chorus_oss.protocol.packets.ContainerClosePacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<org.chorus_oss.protocol.packets.ContainerClosePacket>) {
        val packet = pk.packet

        val containerID: Int = packet.containerID.toInt()

        if (!player.spawned || containerID == SpecialWindowId.PLAYER.id && !player.inventoryOpen) {
            return
        }

        val inventory = player.getWindowById(containerID)

        if (player.windowIndex.containsKey(containerID)) {
            if (containerID == SpecialWindowId.PLAYER.id) {
                player.closingWindowId = containerID
                player.inventory.close(player)
                player.inventoryOpen = false
            } else {
                player.removeWindow(player.windowIndex[containerID]!!)
            }
        }

        if (containerID == -1) {
            player.addWindow(player.craftingGrid, SpecialWindowId.NONE.id)
        }
        if (inventory != null) {
            player.sendPacket(
                org.chorus_oss.protocol.packets.ContainerClosePacket(
                    containerID = containerID.toByte(),
                    containerType = ContainerType(inventory.type),
                    serverInitiatedClose = false,
                )
            )
            player.resetInventory()
        }
    }

    override val packetId: Int = org.chorus_oss.protocol.packets.ContainerClosePacket.id

    companion object : Loggable
}
