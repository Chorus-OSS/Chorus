package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.inventory.SpecialWindowId
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.ContainerClosePacket
import org.chorus_oss.protocol.types.ContainerType

class ContainerCloseProcessor : PacketProcessor<ContainerClosePacket> {
    override fun handle(player: Player, packet: ContainerClosePacket) {
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
                ContainerClosePacket(
                    containerID = containerID.toByte(),
                    containerType = ContainerType(inventory.type),
                    serverInitiatedClose = false,
                )
            )
            player.resetInventory()
        }
    }

    override val packetID: Int = ContainerClosePacket.id

    companion object : Loggable
}
